package org.openjfx

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.geometry.Point2D
import javafx.util.Duration.seconds

//FXGL Imports:
import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.util.BiConsumer
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.components.ExpireCleanComponent
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.components.CollidableComponent
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.physics.box2d.dynamics.BodyType
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef


val isGameOn =  true


enum class CannonType { CANNON, BULLET, BASKET }

class CannonFactory : EntityFactory {

    @Spawns("cannon")
    fun newCannon(data: SpawnData): Entity? {
        return entityBuilder()
            .type(CannonType.CANNON)
            .from(data)
            .view(Rectangle(70.0, 30.0, Color.BROWN))
            //COMMENTED OUT IN ORIG WRKNG JAVA
            // .with( LiftComponent().xAxisSpeedDuration(50.0, seconds(10.0)).yAxisSpeedDuration(250.0, seconds(10.0)) )   //errors! gmknote: orig
            // .with( liftComponent.yAxisDistanceDuration(150.0, seconds(1.0)) )
            .build()
    }

    @Spawns("bullet")
    fun newBullet(data: SpawnData): Entity? {
        val physics = PhysicsComponent()
        physics.setFixtureDef(FixtureDef().density(0.05f))
        physics.setBodyType(BodyType.DYNAMIC)

        physics.setOnPhysicsInitialized {
            val mousePosition: Point2D = getInput().mousePositionWorld
            val pNorm  = Point2D(data.x, data.y).normalize()

            println("pNorm = $pNorm")


            physics.linearVelocity = mousePosition.subtract(data.x.times(-1.05), pNorm.y.times(700.0))


            //GKMOD_1116  TO ABOVE
            //physics.setLinearVelocity(mousePosition.subtract(data.x.times(-1.05), pNorm.y.times(700.0)))
        }

        return entityBuilder()
            .type(CannonType.BULLET)
            .from(data)
            .viewWithBBox(Rectangle(25.0, 25.0, Color.BLUE))
            .with(physics, CollidableComponent(true))
            .with(ExpireCleanComponent(seconds(4.0)))
            .build()
    }

    @Spawns("basketBarrier")   //GMK MODE - REMOVED ? from SpawnData
    fun newBasketBarrier(data: SpawnData): Entity? {
        return entityBuilder()
            .type(CannonType.BASKET)
            .from(data)
            .viewWithBBox(Rectangle(100.0, 300.0, Color.RED))
            .with(PhysicsComponent())
            .build()
    }

    @Spawns("basketGround")
    fun newBasketGround(data: SpawnData): Entity? {  // //GMK MODE - REMOVED ? from SpawnData
        return entityBuilder()
            .type(CannonType.BASKET)
            .from(data)
            .viewWithBBox(Rectangle(300.0, 5.0, Color.TRANSPARENT))
            .with(PhysicsComponent(), CollidableComponent(true))
            .build()
    }
}


class CannonApp : GameApplication () {
    private lateinit var cannon : Entity

    private fun shoot() {
        FXGL.spawn("bullet", cannon.position.add(70.0, 0.0))
    }

    override fun initSettings(settings: GameSettings) {
        with(settings) {
            width   = 800
            height  = 600
            title   = "Cannon (Kotlin)"
            version = "0.2.1"
        }
    }

    override fun initInput() {
        println(">inputInit()")
        onBtnDown(MouseButton.PRIMARY, "Shoot") { shoot() }
    }

    override fun initGameVars( vars: MutableMap<String, Any>) {
        //vars.put("score", 0)  gkmod
        vars["score"] = 0
    }

    override fun initPhysics() {
        FXGL.onCollisionBegin(CannonType.BULLET, CannonType.BASKET, BiConsumer{ bullet: Entity, _ : Entity? ->
            bullet.removeFromWorld()
            FXGL.inc("score", +1000)
        })
    }

    override fun initGame() {
        //showMessage(">initGame()")
        getGameWorld().addEntityFactory(CannonFactory())  //GMK MOD HERE
        initScreenBounds()
        initCannon()
        initBasket()
    }

    //================================================
    private fun initScreenBounds() {
        //getGameWorld().addEntity(Entities.makeScreenBounds(100));   //ALSO COMMENTED OUT IN WORKING J VERS
    }

    private fun initCannon() {
        cannon = getGameWorld().spawn("cannon", 50.0, FXGL.getAppHeight() - 30.toDouble())
    }

    private fun initBasket() {
        FXGL.spawn("basketBarrier", 400.0, FXGL.getAppHeight() - 300.toDouble())
        FXGL.spawn("basketBarrier", 700.0, FXGL.getAppHeight() - 300.toDouble())
        FXGL.spawn("basketGround",  500.0,   FXGL.getAppHeight().toDouble())
    }

    override fun initUI() {
        val scoreText: Text = FXGL.getUIFactory().newText("", Color.BLACK, 24.0)
        //scoreText.setTranslateX(550.0) orig - gmkmod
        //scoreText.translateX = 550.0

        with(scoreText) {
            //scoreText.setTranslateX(550.0) orig - gmkmod
            translateX = 550.0
            translateY = 100.0
            textProperty().bind(FXGL.getGameState().intProperty("score").asString("Score: [%d]"))
        }
        getGameScene().addUINode(scoreText)
    }

}



class MainApp : Application() {

    override fun init() {

        //showMessage(">init()")
        //By default this does nothing, but it
        //can carry out code to set up your app.
        //It runs once before the start method,
        //and after the constructor.
    }

    override fun start(primaryStage: Stage) { // Creating the Java button
        val button = Button()
        // Setting text to button
        button.text = "Hello World (K!)"
        // Registering a handler for button
        button.setOnAction{
            // Printing Hello World! to the console
            println("Hello World!")
        }
        // Initializing the StackPane class
        val root = StackPane()
        // Adding all the nodes to the StackPane
        root.children.add(button)
        // Adding the title to the window (primaryStage)
        primaryStage.title = "Hello World!"
        primaryStage.scene = Scene(root, 300.0, 250.0)
        // Show the window(primaryStage)
        primaryStage.show()
    }

    override fun stop() {
        println("User hit the go-away button?")
        //By default this does nothing
        //It runs if the user clicks the go-away button
        //closing the window or if Platform.exit() is called.
        //Use Platform.exit() instead of System.exit(0).
        //This is where you should offer to save any unsaved
        //stuff that the user may have generated.
    }

}


fun main(args: Array<String>) {

    println("Hallo Kotlin!")

    if (isGameOn) {
        GameApplication.launch(CannonApp::class.java, args)
    } else {
        Application.launch(MainApp::class.java)
    }

}
package net.jyu.smartelevator

import scala.collection.mutable

/**
  * Created by jyu on 4/14/16.
  *
  * For time issue, I just use this one to work as demo drive. console output demo the design idea only, lots of things
  * need to be refined
  */
object Demo extends App{
    println("starting demo ... ...")

    val centralController = new CentralController(3,4)
//    val centralController = new CentralController(1,2) //to test request overflow

    centralController.handleRequest(3,8)
    centralController.handleRequest(4,5)
    centralController.handleRequest(4,12)
    centralController.handleRequest(6,7)
    centralController.handleRequest(6,7) //another request, should add head count
    centralController.handleRequest(6,8) //should assign new cart
    centralController.handleRequest(2,7) //start level lower than all others, should assign new cart


    //below should not
    centralController.handleRequest(7,6)
    centralController.handleRequest(8,1)
    centralController.handleRequest(6,3)
    centralController.displayStatus()


    val e = ElevatorId(3,4,Some(Upward))
    println(s"${e} will stop at : ")
    centralController.reportArrivedStartFlr(e).foreach(println)

    //Assuming inbed software handle stop and open and call back below when mission accomplish
    centralController.reportTaskAccomplish(e)
    centralController.displayStatus()
}

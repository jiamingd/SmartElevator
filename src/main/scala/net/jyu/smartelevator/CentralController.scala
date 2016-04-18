package net.jyu.smartelevator


import scala.collection.mutable
import scala.collection.immutable

/**
  * Created by jyu on 4/14/16.
  *
  *
Assumption:
1. As soon as Elevator opened the door on the starting floor, this elevator is removed from available active Elevators
in application. However, it will only be treated back as idle only when it finished stopping on all assigned floor
2. Cart in code means elevator. Use it for typing convenience
3. System will not consider optimization on picking the idle cart that is nearest to the request's starting floor

  */

class CentralController(val bankCount : Int, val elevCountEachBank : Int) {
  val MAXCOUNT : Int = 5

  var upwardTrack : mutable.Map[Int/* startflr as key */, mutable.Seq[(Int,Int)]] = mutable.HashMap[Int, mutable.Seq[(Int,Int)]]()
  var downwardTrack : mutable.Map[Int, mutable.Seq[(Int,Int)]] = mutable.HashMap[Int, mutable.Seq[(Int,Int)]]()

  var idleCarts : mutable.ListBuffer[ElevatorId] = mutable.ListBuffer[ElevatorId]();
  for(b <- 1 to bankCount; e <- 1 to elevCountEachBank) yield {
    idleCarts += ElevatorId(b,e,None)
  }

  var startFlr2ActiveCartsMap : mutable.Map[Int, ElevatorId] = mutable.HashMap[Int, ElevatorId]()

  private def checkAssignNewCart(direction: MoveDirection, startend: (Int, Int)):Unit ={
    assignNewCart(direction, startend) match{
      case None => println("Sorry, all elevators are fully occupied !!, please retry later")
      case Some(c) =>
    }
  }

  private def assignNewCart(direction: MoveDirection, startend: (Int, Int)) : Option[ElevatorId] = {
    if(idleCarts.size == 0) {
      //Cart maybe all active and to simplify logic, just have user try later
      None
    }else {
      // Create new record for tracking
      direction match {
        case Upward => upwardTrack.put(startend._1,mutable.Seq[(Int,Int)](startend))
        case Downward => downwardTrack.put(startend._1,mutable.Seq[(Int,Int)](startend))
      }

      startFlr2ActiveCartsMap.put(startend._1, idleCarts.remove(idleCarts.size - 1))
      startFlr2ActiveCartsMap.get(startend._1).get.direction = Option(direction)

      startFlr2ActiveCartsMap.get(startend._1)
    }
  }

  def hasActiveCartPoolByDirection(direction:MoveDirection, startend : (Int,Int)):Boolean = {
    direction match {
      case Upward if (upwardTrack.keys.filter(v => v < startend._1).size == 0 ) => false
      case Upward if (upwardTrack.keys.filter(v => v < startend._1).size > 0 ) => true

      case Downward if (downwardTrack.keys.filter(v => v > startend._1).size == 0 ) => false
      case Downward if (downwardTrack.keys.filter(v => v > startend._1).size > 0 ) => true
    }
  }

  def poolLeastOccupiedCart(direction: MoveDirection, startend: (Int, Int)) : Unit={
    // Find the minimun occupied cart among all qualified carts
    var bufCount = MAXCOUNT
    var bufKey : Option[Int] = None
    val candidates =  direction match {
      case Upward => upwardTrack.filter( _._1  < startend._1)
      case Downward => downwardTrack.filter(_._1 > startend._1)
    }

    candidates.foreach( s => {
      if(s._2.size < bufCount){
        bufCount = s._2.size
        bufKey = Some(s._1)
      }})

    bufKey match {
      case None => //assign new cart ,all are fully packed
        checkAssignNewCart(direction, startend)
      case Some(x) => {
        direction match {
          case Upward => upwardTrack.put(x, upwardTrack.get(x).get.+:(startend._1,startend._2))
          case Downward => downwardTrack.put(x, downwardTrack.get(x).get.+:(startend._1,startend._2))
        }
      }
    }
  }

  /**
    * Using starting flr to check if cart can fill in more people. Taking upward scenario, if earlier request is upward 3->9,
    * then come another request 6->12, then combine both persons together and will stop @ [3,6,9,12]. If 2nd come as 1->?,
    * because 1 lower than 3 and cart starting point under the assumption of not being reset, then a new cart will be assigned
    *
    * @param direction
    * @param startend
    */
  private def findCart  (direction: MoveDirection, startend: (Int, Int)): Unit = {
    hasActiveCartPoolByDirection(direction, startend) match {
      case true => poolLeastOccupiedCart(direction, startend)
      case false => checkAssignNewCart(direction, startend)
    }

    //TODO: Assuming here later to plug in some api to notify enlisted elevator to move, not in scope
  }

  def handleRequest(startFlr : Int, endFlr : Int) : Unit = {
    val delta = endFlr - startFlr

    delta match {
      case x if x > 0 => findCart(Upward,(startFlr,endFlr))
      case x if x < 0 => findCart(Downward,(startFlr,endFlr))
      case 0 =>

    }

  }

  def reportArrivedStartFlr(elevator : ElevatorId): List[Int] = {
    startFlr2ActiveCartsMap.find( p => p._2.bankId == elevator.bankId && p._2.cartId == elevator.cartId) match {
      case None => immutable.List()
      case Some(x) => {
        val flrs : mutable.Set[Int] = mutable.HashSet[Int]()

        val track = elevator.direction.get match {
          case Upward => upwardTrack
          case Downward => downwardTrack
        }

        val m = track.get(x._1).get
        m.foreach(mm => {
          flrs.add(mm._1)
          flrs.add(mm._2)
        })

        val out = elevator.direction.get match {
          case Upward => flrs.toList.sortWith(_<_)
          case Downward => flrs.toList.sortWith(_>_)
        }

        // remove from track  && map
        track -= x._1



        startFlr2ActiveCartsMap.remove(x._1)
        out
      }
    }


  }

  def reportTaskAccomplish(elevator: ElevatorId):Unit = {
    // recycle elevator
    idleCarts += elevator
  }

  def displayStatus():Unit={
    println("\t\t STATUS \t\t")
    println("Active carts and assigned start/end floor:")
    startFlr2ActiveCartsMap.foreach(println)
    println("upward request pool:")
    upwardTrack.foreach(println)
    println("downward request pool:")
    downwardTrack.foreach(println)


  }

}

trait  MoveDirection
case object Upward extends MoveDirection
case object Downward extends MoveDirection

case class ElevatorId(bankId:Int, cartId:Int, var direction:Option[MoveDirection])

class Elevator(val centralController: CentralController, id: ElevatorId){

}
package models

import scala.collection.mutable.ArrayBuffer
import akka.actor._
import akka.actor.Actor._
import akka.actor.Props
import akka.event.Logging
import akka.util.duration._
import java.util.concurrent.TimeUnit
import akka.util._
import akka.dispatch.{Await, Future, ExecutionContext} 
import akka.pattern._
import play.api.libs.concurrent._
import java.util.concurrent.Executors

object Take
object Taken
object Put
object Busy
object Eat
object Think

case class WatchMe(ref: ActorRef)
case class Terminated(ref: ActorRef)
case class Message(s: String)

 
abstract class Reaper extends Actor {

  // Keep track of what we're watching
  val watched = ArrayBuffer.empty[ActorRef]
 
  // Derivations need to implement this method.  It's the
  // hook that's called when everything's dead
  def allSoulsReaped(): Unit
 
  // Watch and check for termination
  final def receive = {
    case WatchMe(ref) =>
      context.watch(ref)
      watched += ref
    case Terminated(ref) =>
      watched -= ref
      if (watched.isEmpty) allSoulsReaped()
  }
}

class TestReaper extends Reaper {
  def allSoulsReaped(): Unit = context.system.shutdown()
}

class Philosopher(name: String, left: ActorRef, right: ActorRef) extends Actor {
	
	ChatRoom.default ! Talk(name,  " has " + left + " and " + right)

	def thinking:Receive = {
		
		case Eat =>
			left ! Take
			right ! Take

		case Busy =>
			context.become(denied)

		case Taken =>
			if(sender == left){
				context.become(waitingfor(right, left))
				ChatRoom.default ! Talk(name, " is waiting for " + right)
			}
			else{
				context.become(waitingfor(left, right))				
				ChatRoom.default ! Talk(name, " is waiting for " + left)
			}
	}

	def eating:Receive = {

		case Think =>
			context.become(thinking)
   			ChatRoom.default ! Talk(name, " is thinking.")
		    left ! Put
		    right ! Put
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def waitingfor(chopstick: ActorRef, otherstick: ActorRef):Receive = {
		case Taken =>
			context.become(eating)
			ChatRoom.default ! Talk(name, " got both chopsticks and is now eating.")
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Think)
		case Busy =>
			context.become(thinking)
			ChatRoom.default ! Talk(name, " is thinking.")
			otherstick ! Put
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def denied:Receive = {
		case Taken =>
			ChatRoom.default ! Talk(name, " was denied!")
			sender ! Put
			context.become(thinking)
			ChatRoom.default ! Talk(name, " is thinking.")
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)

		case Busy =>
			ChatRoom.default ! Talk(name, " was denied!")
			context.become(thinking)
			ChatRoom.default ! Talk(name, " is thinking again.")
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def receive = thinking
}

class Chopstick(number: Int) extends Actor {
	def free:Receive = {
		case Take =>
			ChatRoom.default ! Talk("chopstick " + number, " is now taken by " + sender)
			sender ! Taken
			context.become(takenby(sender))
	}

	def takenby(phil: ActorRef):Receive = {
		case Take =>
			ChatRoom.default ! Talk("chopstick " +  number, " was requested by " + sender + " but is already taken by " + phil + ".")
			sender ! Busy

		case Put =>
		    ChatRoom.default ! Talk("chopstick " + number, " was put back by " + sender)
			context.become(free)
	}

	def receive = free
}

object Dining {
	
	val system = ActorSystem("DiningSystem")
	var chopsticks:IndexedSeq[ActorRef] = _
	var philosophers:List[ActorRef] = _
	var reaper:ActorRef = _

	def runDinings = {
		ChatRoom.default ! Talk("Dinner", " is served.")
		chopsticks = for(i <- 1 to 5) yield system.actorOf(Props(new Chopstick(i)), i.toString)
		philosophers = for((name, i) <- List("A","B","C","D","E").zipWithIndex) yield system.actorOf(Props(new Philosopher(name,chopsticks(i), chopsticks((i+1) % 5))),name) 
		philosophers.foreach(_ ! Eat)
		reaper = system.actorOf(Props(new TestReaper()))
		philosophers.foreach(reaper ! WatchMe(_))
		chopsticks.foreach(reaper ! WatchMe(_))
	}

	def stopDining = {
		if (philosophers.isEmpty)
			ChatRoom.default ! Talk("Restaurant", " has not started serving yet.")
		else{
			ChatRoom.default ! Talk("Restaurant", " is closed.")
			philosophers.foreach(_ ! PoisonPill)
			chopsticks.foreach(_ ! PoisonPill)
			philosophers = Nil
		}
	}
}
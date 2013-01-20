package controllers

import akka.actor._
import akka.actor.Actor._
import akka.actor.Props
import akka.event.Logging
import akka.util.duration._
import java.util.concurrent.TimeUnit
import akka.util._
//import scala.concurrent.duration._
import akka.pattern._
import play.api.libs.concurrent._

object Take
object Taken
object Put
object Busy
object Eat
object Think
case class Message(s: String)

class Philosopher(name: String, left: ActorRef, right: ActorRef, restaurant: ActorRef) extends Actor {
	restaurant ! name + " has " + left + " and " + right
	
	def thinking:Receive = {
		
		case Eat =>
			left ! Take
			right ! Take

		case Busy =>
			context.become(denied)

		case Taken =>
			if(sender == left){
				context.become(waitingfor(right, left))
				restaurant ! name + " is waiting for " + right
			}
			else{
				context.become(waitingfor(left, right))				
				restaurant ! name + " is waiting for " + left
			}
	}

	def eating:Receive = {

		case Think =>
			context.become(thinking)
		    restaurant ! name + " is thinking."
		    left ! Put
		    right ! Put
	}

	def waitingfor(chopstick: ActorRef, otherstick: ActorRef):Receive = {
		case Taken =>
			context.become(eating)
			restaurant ! name + " got both chopsticks and is now eating."
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Think)
		case Busy =>
			context.become(thinking)
			restaurant ! name + " is thinking."
			otherstick ! Put
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def denied:Receive = {
		case Taken =>
			restaurant ! name + " was denied!"
			sender ! Put
			context.become(thinking)
			restaurant ! name + " is thinking."
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
		case Busy =>
			restaurant ! name + " was denied!"
			context.become(thinking)
			restaurant ! name + " is thinking."
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def receive = thinking
}

class Chopstick(number: Int, restaurant: ActorRef) extends Actor {
	def free:Receive = {
		case Take =>
			restaurant ! number + " is now taken by " + sender
			sender ! Taken
			context.become(takenby(sender))
	}

	def takenby(phil: ActorRef):Receive = {
		case Take =>
			restaurant ! number + " was requested by " + sender + " but is already taken by " + phil + "."
			sender ! Busy

		case Put =>
			restaurant ! number + " was put back by " + sender
			context.become(free)
	}

	def receive = free
}
object Dump

class Restaurant extends Actor {

    var messages:List[String] = List()

	def mainStream:Receive = {
		case Dump =>
			val data:Array[Byte] = messages.flatMap(msg => msg.toCharArray.map(_.toByte)).toArray//messages.foreach(msg => msg.toCharArray.map(_.toByte))
			
			//println(new String(data))
			sender ! data
			messages = List()//empty list
			//actorStream |>> Iteratee.forEach[String](s => println(s))
		case msg:String =>
			//println(msg)
			messages = msg :: messages
	}
	def toHexString(bytes:Array[Byte]) : String = {
      val sb = new StringBuilder();
      for(i <- 0 until bytes.length){
        sb.append("%02x".format(bytes(i)))
      }
      return sb.toString()
    }

	def receive = mainStream
}

object Dining {
	
	val system = ActorSystem("DiningSystem")

	def runDinings = {
		val restaurant = system.actorOf(Props(new Restaurant()))
		val chopsticks = for(i <- 1 to 5) yield system.actorOf(Props(new Chopstick(i, restaurant)), i.toString)
		val philosophers = for((name, i) <- List("A","B","C","D","E").zipWithIndex) yield system.actorOf(Props(new Philosopher(name,chopsticks(i), chopsticks((i+1) % 5),restaurant)),name) 
		philosophers.foreach(_ ! Eat)
		restaurant ! "Dinner is served."
		//restaurantObj.actorStream

		val actorStream = { () =>
			//Promise.timeout(Some(restarant ? Dump), 100 milliseconds)
			(restaurant ? Dump)(5 seconds).mapTo[Option[Array[Byte]]].asPromise//.asPromise
			//return new Promise(new String(), 100);
		}
		actorStream
	}

	def stopDining = {
		system.shutdown
	}
}
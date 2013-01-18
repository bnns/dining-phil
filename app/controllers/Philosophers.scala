package controllers

import akka.actor._
import akka.actor.Actor._
import akka.actor.Props
import akka.event.Logging
import akka.util.Duration
import java.util.concurrent.TimeUnit

object Take
object Taken
object Put
object Busy
object Eat
object Think

class Philosopher(name: String, left: ActorRef, right: ActorRef) extends Actor {
	println(name + " has " + left + " and " + right)
	
	def thinking:Receive = {
		
		case Eat =>
			left ! Take
			right ! Take

		case Busy =>
			context.become(denied)

		case Taken =>
			if(sender == left){
				context.become(waitingfor(right, left))
				println(name + " is waiting for " + right)
			}
			else{
				context.become(waitingfor(left, right))				
				println(name + " is waiting for " + left)
			}
	}

	def eating:Receive = {

		case Think =>
			context.become(thinking)
		    println(name + " is thinking.")
		    left ! Put
		    right ! Put
	}

	def waitingfor(chopstick: ActorRef, otherstick: ActorRef):Receive = {
		case Taken =>
			context.become(eating)
			println(name + " got both chopsticks and is now eating.")
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Think)
		case Busy =>
			context.become(thinking)
			println(name + " is thinking.")
			otherstick ! Put
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def denied:Receive = {
		case Taken =>
			println(name + " was denied!")
			sender ! Put
			context.become(thinking)
			println(name + " is thinking.")
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
		case Busy =>
			println(name + " was denied!")
			context.become(thinking)
			println(name + " is thinking.")
			context.system.scheduler.scheduleOnce(Duration(5, "seconds"), self, Eat)
	}

	def receive = thinking
}

class Chopstick(number: Int) extends Actor{
	def free:Receive = {
		case Take =>
			println(number + " is now taken by " + sender)
			sender ! Taken
			context.become(takenby(sender))
	}

	def takenby(phil: ActorRef):Receive = {
		case Take =>
			println(number + " was requested by " + sender + " but is already taken by " + phil + ".")
			sender ! Busy

		case Put =>
			println(number + " was put back by " + sender)
			context.become(free)
	}

	def receive = free
}

object Dining {

	def runDining = {
		val system = ActorSystem("DiningSystem")
		val chopsticks = for(i <- 1 to 5) yield system.actorOf(Props(new Chopstick(i)), i.toString)
		val philosophers = for((name, i) <- List("A","B","C","D","E").zipWithIndex) yield system.actorOf(Props(new Philosopher(name,chopsticks(i), chopsticks((i+1) % 5))),name) 
		philosophers.foreach(_ ! Eat)
	}
}
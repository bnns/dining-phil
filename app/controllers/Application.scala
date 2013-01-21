package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json
import play.api.libs.iteratee._
import models._
<<<<<<< HEAD
import akka.dispatch.Await
import akka.dispatch.Future
=======
import play.api.libs.Comet

import akka.dispatch.Await
import akka.dispatch.Future
import java.util.concurrent.TimeUnit
import akka.util._
import akka.util.duration._

>>>>>>> b58c6e3ac98ddb688f14374d2a802c0614a734b5

object Application extends Controller {
  
   val data = Dining.runDinings

	def index = Action {
<<<<<<< HEAD
 		Ok(views.html.index("simulation:"))
=======
		/*val data = Dining.runDinings
		val dataContent:Enumerator[String] = Enumerator.fromCallback(data)
		//for(i <- dataContent){println(i)}
		//Iteratee.foreach[Array[Byte]](msg => println(new String(msg)))

		//Ok.stream(dataContent.andThen(Enumerator.eof) &> Comet(callback = "console.log"))
		Ok.stream(dataContent.andThen(Enumerator.eof))*/
		Ok("test")
>>>>>>> b58c6e3ac98ddb688f14374d2a802c0614a734b5
	}
	
	def start =  {
		val data = Dining.runDinings
		val dataContent:Enumerator[List[String]] = Enumerator.fromCallback(data)
		Ok("Start")
	}

	def stop = {
		Dining.stopDining
	}
<<<<<<< HEAD
	
	def act = Action {// implicit request =>
		//val dataContent:Enumerator[List[String]] = Enumerator.fromCallback(data)
		//val result = Await.result(data, timeout.duration)//.asInstanceOf[Strings]
        //println(result)
		val test = List("Hello World", "Test", "This")
		Ok(json.Json.toJson(test))
=======
	val data = Dining.runDinings
	def act = Action { implicit request =>
		val timeout = Timeout(10 seconds)
		val result = Await.result(data().mapTo[List[String]], timeout.duration)//.asInstanceOf[Strings]
		println(result) 

		// to get a Blah object from request content
		val blah = json.Json.toJson(result).as[List[String]]
	
		// to return Blah as application/json, you just have to convert your Blah to a JsValue and give it to Ok()
		Ok(json.Json.toJson(blah))
>>>>>>> b58c6e3ac98ddb688f14374d2a802c0614a734b5
	}

}

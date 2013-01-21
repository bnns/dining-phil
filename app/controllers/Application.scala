package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent._
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json
import play.api.libs.iteratee.Enumerator
import models._
import akka.dispatch.Await
import akka.dispatch.Future

object Application extends Controller {
  
   val data = Dining.runDinings

	def index = Action {
 		Ok(views.html.index("simulation:"))
	}
	
	def start =  {
		val data = Dining.runDinings
		val dataContent:Enumerator[List[String]] = Enumerator.fromCallback(data)
		Ok("Start")
	}

	def stop = {
		Dining.stopDining
	}
	
	def act = Action {// implicit request =>
		//val dataContent:Enumerator[List[String]] = Enumerator.fromCallback(data)
		//val result = Await.result(data, timeout.duration)//.asInstanceOf[Strings]
        //println(result)
		val test = List("Hello World", "Test", "This")
		Ok(json.Json.toJson(test))
	}

}
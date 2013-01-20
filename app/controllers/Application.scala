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

object Application extends Controller {
  
	def index = Action {
		val data = Dining.runDinings
		val dataContent:Enumerator[Array[Byte]] = Enumerator.fromCallback(data)
		Ok.stream(/*dataContent.andThen(Enumerator.eof)*/views.html.index("test"))		
	}
	
	def start = Action {
		val data = Dining.runDinings
		val dataContent:Enumerator[Array[Byte]] = Enumerator.fromCallback(data)
		Ok.stream(dataContent.andThen(Enumerator.eof))
	}
	
	def chatRoom(username: Option[String]) = Action { implicit request =>
    	username.filterNot(_.isEmpty).map { username =>
      		Ok(views.html.chatRoom(username))
    	}.getOrElse {
      		Redirect(routes.Application.index).flashing(
        		"error" -> "Please choose a valid username."
      		)
    	}
  	}

	def stop = Action {
		Dining.stopDining
		Ok("End")
	}
	
	def act = Action { implicit request =>
		val items = json.Json.parse(request.body.asText.get).as[String]	
		Ok(json.Json.toJson(items))
	}

}

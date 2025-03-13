package routes

import (
	"waiter-assist/controllers"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {

	//Route pour le login
	r.POST("/login", controllers.Login)
	r.GET("/mqtt", controllers.GetMQTTId)

	users := r.Group("/users")
	{
		users.POST("", controllers.CreateUser)
	}

}

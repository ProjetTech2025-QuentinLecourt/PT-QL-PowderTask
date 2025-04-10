package routes

import (
	"waiter-assist/controllers"
	"waiter-assist/midllewares"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {

	//Route pour le login
	r.POST("/login", controllers.Login)
	r.GET("/mqtt", midllewares.AuthMiddleware(), controllers.GetMQTTId)

	users := r.Group("/users")
	{
		users.POST("", controllers.CreateUser)
	}

}

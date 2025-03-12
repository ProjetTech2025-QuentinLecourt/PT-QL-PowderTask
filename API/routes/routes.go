package routes

import (
	"waiter-assist/controllers"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {

	//Route pour le login
	r.POST("/login", controllers.Login)

	users := r.Group("/users")
	{
		users.POST("", controllers.CreateUser)
	}

}

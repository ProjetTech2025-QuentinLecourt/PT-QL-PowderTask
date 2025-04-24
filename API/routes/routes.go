package routes

import (
	"waiter-assist/controllers"
	"waiter-assist/midllewares"

	"github.com/gin-gonic/gin"
)

const (
	ChefEquipe          = "CE"
	Poudreur            = "P"
	OperateurProduction = "OP"
)

func RegisterRoutes(r *gin.Engine) {

	//Route pour le login
	r.POST("/login", controllers.Login)
	r.GET("/mqtt", midllewares.AuthMiddleware(), controllers.GetMQTTId)
	r.GET("/machines", midllewares.AuthMiddleware(), controllers.GetMachines)

	users := r.Group("/users")
	{
		users.POST("", controllers.CreateUser)
	}

	machine := r.Group("/machine")
	{
		machine.GET("/:id/users", midllewares.AuthMiddleware(ChefEquipe), controllers.GetMachineUsers)
	}

}

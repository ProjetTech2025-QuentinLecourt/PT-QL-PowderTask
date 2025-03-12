package models

import "github.com/dgrijalva/jwt-go"

// Claims représente la structure du JWT
type Claims struct {
	Email string `json:"email"`
	jwt.StandardClaims
}

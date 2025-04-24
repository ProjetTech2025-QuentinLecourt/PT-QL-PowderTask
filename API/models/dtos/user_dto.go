package dtos

type UserDto struct {
	Id        uint   `json:"id"`
	FirstName string `json:"first_name"`
	LastName  string `json:"last_name"`
	Email     string `json:"email"`
	Password  string `json:"password"`
	Job       string `json:"job"`
}

type UserDtoResponse struct {
	FirstName string `json:"first_name"`
	LastName  string `json:"last_name"`
	Email     string `json:"email"`
}

type UserDtoResponseForList struct {
	FirstName string `json:"first_name"`
	LastName  string `json:"last_name"`
}

type UserDtoLogin struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

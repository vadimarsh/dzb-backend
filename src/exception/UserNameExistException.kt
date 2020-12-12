package com.example.exception

import java.lang.RuntimeException

class UserNameExistException(message: String) : RuntimeException(message)
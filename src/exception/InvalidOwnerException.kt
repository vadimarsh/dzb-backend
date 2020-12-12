package com.example.exception

import java.lang.RuntimeException

class InvalidOwnerException() : RuntimeException("This is not your post") {


}

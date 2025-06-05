package com.example.kotlin_amateur.exception

sealed class ProfileException(message: String) : RuntimeException(message)

class ImageSaveFailedException(message: String = "이미지 저장 실패") : ProfileException(message)
class TokenNotFoundException(message: String = "엑세스 토큰 없음") : ProfileException(message)
class ProfileLoadFailedException(message: String) : ProfileException(message)
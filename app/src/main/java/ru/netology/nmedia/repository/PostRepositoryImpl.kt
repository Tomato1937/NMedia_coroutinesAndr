package ru.netology.nmedia.repository

import androidx.lifecycle.*
import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(PostEntity.fromDto(body))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                dao.removeById(id)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = PostsApi.service.getById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                dao.likeById(id)
                if (response.body()!!.likedByMe) {
                    PostsApi.service.unlikeById(id)
                    val body = (response.body())?.copy(
                        likedByMe = false,
                        likes = (response.body())!!.likes - 1
                    ) ?: throw ApiError(response.code(), response.message())
                    dao.insert(PostEntity.fromDto(body))
                } else {
                    PostsApi.service.likeById(id)
                    val body = (response.body())?.copy(
                        likedByMe = true,
                        likes = (response.body())!!.likes + 1
                    ) ?: throw ApiError(response.code(), response.message())
                    dao.insert(PostEntity.fromDto(body))
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}

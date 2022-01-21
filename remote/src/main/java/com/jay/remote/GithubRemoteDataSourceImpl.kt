package com.jay.remote

import com.jay.data.model.DataUser
import com.jay.data.remote.GithubRemoteDataSource
import com.jay.remote.api.GithubApi
import com.jay.remote.mapper.GithubRemoteMapper
import io.reactivex.Single
import javax.inject.Inject

class GithubRemoteDataSourceImpl @Inject constructor(private val githubApi: GithubApi) : GithubRemoteDataSource {

    override fun searchUser(name: String): Single<List<DataUser>> {
        return githubApi.searchUser(name)
            .map { it.items?.map(GithubRemoteMapper::mapToData) }
    }

}
package iad1tya.echo.music.spotify

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import iad1tya.echo.music.utils.dataStore
import kotlinx.coroutines.flow.first
@Serializable
data class SpotifyAppToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

/**
 * Handles communication with the Spotify API.
 */
object SpotifyApi {
    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private var currentToken: String? = null
    private var tokenExpirationMs: Long = 0L

    private val DEFAULT_CLIENT_ID = "ddc77f226d7e45699073cfc5894bb42c"
    private val DEFAULT_CLIENT_SECRET = "68eb4003c7ba4e078e48cd3e93189c7f"

    private var appContext: android.content.Context? = null

    fun init(context: android.content.Context) {
        appContext = context.applicationContext
    }

    /**
     * Fetches an app token using the Client Credentials Flow.
     * This is extremely stable and ensures the Spotify API works reliably.
     */
    suspend fun getAnonymousToken(): String? {
        if (currentToken != null && System.currentTimeMillis() < tokenExpirationMs - 60000) {
            return currentToken
        }

        return try {
            var clientId = DEFAULT_CLIENT_ID
            var clientSecret = DEFAULT_CLIENT_SECRET

            appContext?.let { ctx ->
                val prefs = ctx.dataStore.data.first()
                val userClientId = prefs[iad1tya.echo.music.constants.SpotifyClientIdKey]
                val userClientSecret = prefs[iad1tya.echo.music.constants.SpotifyClientSecretKey]
                if (!userClientId.isNullOrBlank() && !userClientSecret.isNullOrBlank()) {
                    clientId = userClientId.trim()
                    clientSecret = userClientSecret.trim()
                }
            }

            val authString = "$clientId:$clientSecret"
            val base64Auth = java.util.Base64.getEncoder().encodeToString(authString.toByteArray(Charsets.UTF_8))
            
            val response: SpotifyAppToken = client.post("https://accounts.spotify.com/api/token") {
                header("Authorization", "Basic $base64Auth")
                header("Content-Type", "application/x-www-form-urlencoded")
                setBody("grant_type=client_credentials")
            }.body()
            
            currentToken = response.access_token
            tokenExpirationMs = System.currentTimeMillis() + (response.expires_in * 1000L)
            currentToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchPlaylists(query: String): List<SpotifyPlaylist> {
        val token = getAnonymousToken() ?: return emptyList()
        return try {
            val response: SpotifySearchResponse = client.get("https://api.spotify.com/v1/search") {
                parameter("q", query)
                parameter("type", "playlist")
                parameter("limit", 20)
                header("Authorization", "Bearer $token")
            }.body()
            
            response.playlists?.items?.map { it.toSpotifyPlaylist() } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFeaturedPlaylists(): List<SpotifyPlaylist> {
        val token = getAnonymousToken() ?: return emptyList()
        return try {
            val response: SpotifyFeaturedPlaylistsResponse = client.get("https://api.spotify.com/v1/browse/featured-playlists") {
                parameter("limit", 15)
                header("Authorization", "Bearer $token")
            }.body()
            
            response.playlists.items.map { it.toSpotifyPlaylist() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPlaylistTracks(playlistId: String): List<SpotifyTrack> {
        val token = getAnonymousToken() ?: return emptyList()
        return try {
            val response: SpotifyPlaylistTracksResponse = client.get("https://api.spotify.com/v1/playlists/$playlistId/tracks") {
                parameter("limit", 50)
                header("Authorization", "Bearer $token")
            }.body()
            
            response.items.mapNotNull { it.track?.toSpotifyTrack() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Serializable
data class SpotifySearchResponse(val playlists: SpotifyPagingObject<SpotifyPlaylistItem>? = null)

@Serializable
data class SpotifyFeaturedPlaylistsResponse(val playlists: SpotifyPagingObject<SpotifyPlaylistItem>)

@Serializable
data class SpotifyPagingObject<T>(val items: List<T>)

@Serializable
data class SpotifyPlaylistItem(
    val id: String,
    val name: String,
    val owner: SpotifyOwner? = null,
    val images: List<SpotifyImage>? = null
) {
    fun toSpotifyPlaylist() = SpotifyPlaylist(
        id = id,
        title = name,
        owner = owner?.display_name ?: "Spotify",
        thumbnailUrl = images?.firstOrNull()?.url ?: ""
    )
}

@Serializable
data class SpotifyOwner(val display_name: String? = null)

@Serializable
data class SpotifyImage(val url: String)

@Serializable
data class SpotifyPlaylistTracksResponse(val items: List<SpotifyPlaylistItemTrack>)

@Serializable
data class SpotifyPlaylistItemTrack(val track: SpotifyTrackItem? = null)

@Serializable
data class SpotifyTrackItem(
    val id: String? = null,
    val name: String? = null,
    val artists: List<SpotifyArtist>? = null,
    val album: SpotifyAlbum? = null,
    val duration_ms: Long? = null
) {
    fun toSpotifyTrack(): SpotifyTrack? {
        if (id == null || name == null) return null
        return SpotifyTrack(
            id = id,
            title = name,
            artist = artists?.firstOrNull()?.name ?: "Unknown Artist",
            thumbnailUrl = album?.images?.firstOrNull()?.url ?: "",
            durationMs = duration_ms ?: 0L
        )
    }
}

@Serializable
data class SpotifyArtist(val name: String)

@Serializable
data class SpotifyAlbum(val images: List<SpotifyImage>? = null)

data class SpotifyPlaylist(
    val id: String,
    val title: String,
    val owner: String,
    val thumbnailUrl: String
)

data class SpotifyTrack(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationMs: Long
)

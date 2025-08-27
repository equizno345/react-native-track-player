package com.doublesymmetry.trackplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.doublesymmetry.trackplayer.service.MusicService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Utility object that allows controlling the player from native Android code
 * such as widgets. It mirrors the behaviour of the React Native `setupPlayer`
 * call so the player can be initialised without a React instance.
 */
object TrackPlayer : ServiceConnection {
    private var isServiceBound = false
    private var playerOptions: Bundle? = null
    private val scope = MainScope()
    private lateinit var musicService: MusicService
    private var setupCallback: (() -> Unit)? = null

    /**
     * Bind to the [MusicService] and initialise the player.
     * If the player has already been set up this call does nothing.
     */
    @JvmStatic
    fun setupPlayer(context: Context, options: Bundle? = null, onSetupComplete: (() -> Unit)? = null) {
        if (isServiceBound) {
            onSetupComplete?.let { scope.launch { it() } }
            return
        }

        playerOptions = options
        setupCallback = onSetupComplete

        Intent(context, MusicService::class.java).also { intent ->
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
            val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
            MediaBrowser.Builder(context, sessionToken).buildAsync()
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        scope.launch {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
            musicService = binder.service
            musicService.setupPlayer(playerOptions)
            isServiceBound = true
            setupCallback?.invoke()
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        scope.launch {
            isServiceBound = false
        }
    }
}


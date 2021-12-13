package com.xiaoma.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.xiaoma.frameanim.FrameAnimFactory
import com.xiaoma.frameanim.FrameAnimSurfaceView
import com.xiaoma.frameanim.anim.FrameAnimListener
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.costant.ResourceType

class MainActivity : AppCompatActivity() {

    private lateinit var frameAnimTextureView: FrameAnimSurfaceView
    private val frameIds: IntArray = intArrayOf(
        R.mipmap.dance_a_0001,
        R.mipmap.dance_a_0002,
        R.mipmap.dance_a_0003,
        R.mipmap.dance_a_0004,
        R.mipmap.dance_a_0005,
        R.mipmap.dance_a_0006,
        R.mipmap.dance_a_0007,
        R.mipmap.dance_a_0008,
        R.mipmap.dance_a_0009,
        R.mipmap.dance_a_0010,
        R.mipmap.dance_a_0011,
        R.mipmap.dance_a_0012,
        R.mipmap.dance_a_0013,
        R.mipmap.dance_a_0014,
        R.mipmap.dance_a_0015,
        R.mipmap.dance_a_0016,
        R.mipmap.dance_a_0017,
        R.mipmap.dance_a_0018,
        R.mipmap.dance_a_0019,
        R.mipmap.dance_a_0020,
        R.mipmap.dance_a_0021,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        frameAnimTextureView = findViewById(R.id.frame_animation_texture_view)

        findViewById<Button>(R.id.btn_load_asset_sprite).setOnClickListener {
            loadAssetSprite()
        }

        findViewById<Button>(R.id.btn_load_resource_frame_animation).setOnClickListener {
            loadResourceFrameAnimation()
        }
    }

    private fun loadResourceFrameAnimation() {
        val resourceAnim: IFrameAnim = FrameAnimFactory.createWithResIds(this, frameIds)
        loadFrameAnim(resourceAnim)
    }

    private fun loadAssetSprite() {
        val spriteAnim: IFrameAnim =
            FrameAnimFactory.createWithSprite(this, "sprites/", ResourceType.ASSETS)
        loadFrameAnim(spriteAnim)
    }

    private fun loadFrameAnim(spriteAnim: IFrameAnim) {
        frameAnimTextureView.stopAllAnim()
        spriteAnim.oneshot = false
        spriteAnim.addFrameAnimListener(object : FrameAnimListener {
            override fun onStart(anim: IFrameAnim) {

            }

            override fun onStop(anim: IFrameAnim) {

            }

        })
        frameAnimTextureView.addFrameAnim(spriteAnim)
        frameAnimTextureView.startAllAnim()
    }
}
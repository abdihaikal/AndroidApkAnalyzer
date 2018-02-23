package sk.styk.martin.apkanalyzer.ui.activity.appdetail.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_app_detail.*
import sk.styk.martin.apkanalyzer.R
import sk.styk.martin.apkanalyzer.ui.activity.appdetail.pager.AppDetailPagerContract.Companion.ARG_PACKAGE_NAME
import sk.styk.martin.apkanalyzer.ui.activity.appdetail.pager.AppDetailPagerContract.Companion.ARG_PACKAGE_PATH
import sk.styk.martin.apkanalyzer.ui.activity.appdetail.pager.AppDetailPagerFragment

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [AppListFragment].
 *
 * @author Martin Styk
 */
class AppDetailActivity : AppCompatActivity(), AppDetailActivityContract.View {

    private lateinit var presenter: AppDetailActivityContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_detail)

        presenter = AppDetailActivityPresenter()
        presenter.view = this
        presenter.initialize()
    }

    override fun setupViews() {
        setSupportActionBar(detail_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val existingAppDetailPagerFragment = supportFragmentManager.findFragmentByTag(AppDetailPagerFragment.TAG)
        if (existingAppDetailPagerFragment == null) {

            val detailFragment = AppDetailPagerFragment.create(
                    packageName = intent.getStringExtra(ARG_PACKAGE_NAME),
                    packagePath = intent.getStringExtra(ARG_PACKAGE_PATH))

            supportFragmentManager.beginTransaction().add(sk.styk.martin.apkanalyzer.R.id.item_detail_container, detailFragment, AppDetailPagerFragment.TAG)
                    .commit()
        }

        // this happens only in tablet mode when this activity is rotated from horizontal to vertical orientation
        if (btn_actions == null) {
            app_bar.setExpanded(false)
        } else {
            btn_actions?.let {
                it.setOnClickListener { _ ->
                    //delegate to fragment
                    (supportFragmentManager.findFragmentByTag(AppDetailPagerFragment.TAG) as AppDetailPagerFragment).presenter.actionButtonClick()
                }
            }
        }

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713")
        val adRequest = AdRequest.Builder()
                .addTestDevice("72FEA8FEF46331E756C654CF5C76557C")
                .build()

        ad_view?.loadAd(adRequest)
        ad_view?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                ad_view?.visibility = View.VISIBLE
                val displayHeight = resources.displayMetrics.heightPixels / resources.displayMetrics.density
                val bannerHeight = when {
                    displayHeight <= 400 -> resources.getDimensionPixelOffset(R.dimen.ad_banner_height_small)
                    displayHeight > 400 && displayHeight < 720 -> resources.getDimensionPixelOffset(R.dimen.ad_banner_height_medium)
                    else -> resources.getDimensionPixelOffset(R.dimen.ad_banner_height_large)
                }

                item_detail_container.setPadding(0, 0, 0, bannerHeight)
            }
        }
    }

    public override fun onPause() {
        ad_view?.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        ad_view?.resume()
    }

    public override fun onDestroy() {
        ad_view?.destroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun createIntent(packageName: String? = null, packagePath: String? = null, context: Context): Intent {
            val intent = Intent(context, AppDetailActivity::class.java)
            intent.putExtra(ARG_PACKAGE_NAME, packageName)
            intent.putExtra(ARG_PACKAGE_PATH, packagePath)

            return intent
        }
    }
}


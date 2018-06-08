package com.shortstack.hackertracker.ui.activities


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.stkent.amplify.tracking.Amplify
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.App
import com.shortstack.hackertracker.BuildConfig
import com.shortstack.hackertracker.R
import com.shortstack.hackertracker.analytics.AnalyticsController
import com.shortstack.hackertracker.database.DatabaseManager
import com.shortstack.hackertracker.ui.MainActivityViewModel
import com.shortstack.hackertracker.ui.ReviewBottomSheet
import com.shortstack.hackertracker.ui.schedule.EventBottomSheet
import com.shortstack.hackertracker.utils.SharedPreferencesUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.row_nav_view.*
import kotlinx.android.synthetic.main.view_filter.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var storage: SharedPreferencesUtil

    @Inject
    lateinit var database: DatabaseManager

    @Inject
    lateinit var analytics: AnalyticsController

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        App.application.component.inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setupNavigation()

        val mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        mainActivityViewModel.conference.observe(this, Observer {
            if (it != null) {
                nav_view.getHeaderView(0).nav_title.text = it.conference.title
            }
        })
        mainActivityViewModel.conferences.observe(this, Observer {

            nav_view.menu.removeGroup(R.id.nav_cons)

            it?.forEach {
                nav_view.menu.add(R.id.nav_cons, it.index, 0, it.title).apply {
                    isChecked = it.isSelected
                    icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_chevron_right_white_24dp)
                }
            }
        })

        database.typesLiveData.observe(this, Observer {
            filters.setTypes(it)
        })

        filter.setOnClickListener { onFilterClick() }
        close.setOnClickListener { onFilterClick() }

        if (savedInstanceState == null) {
            if (Amplify.getSharedInstance().shouldPrompt() && !BuildConfig.DEBUG) {
                val review = ReviewBottomSheet.newInstance()
                review.show(this.supportFragmentManager, review.tag)
            }
        }

        // TODO: Remove, this is only for debugging.
        Logger.d("Created MainActivity " + (System.currentTimeMillis() - App.application.timeToLaunch))

    }

    private fun setupNavigation() {
        navController = findNavController(R.id.mainNavigationFragment)
        setupActionBarWithNavController(navController, drawerLayout = drawer_layout)

        navController.addOnNavigatedListener { _, destination ->
            //            val visibility = if (destination.id == R.id.nav_schedule) View.VISIBLE else View.INVISIBLE
//            setFABVisibility(visibility)
        }

        initNavDrawer()
    }

    private fun initNavDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.mainNavigationFragment).navigateUp()

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(R.style.AppTheme, true)
        return theme
    }

    private fun onFilterClick() {
//        toggleFAB(onClick = true)
        toggleFilters()
    }

    private fun toggleFilters() {

        val position = IntArray(2)

        filter.getLocationOnScreen(position)

        val (cx, cy) = position

        val radius = Math.hypot(cx.toDouble(), cy.toDouble())

        if (filters.visibility == View.INVISIBLE) {


            val anim = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewAnimationUtils.createCircularReveal(filters, cx, cy, 0f, radius.toFloat())
            } else {
                null
            }

            filters.visibility = View.VISIBLE

            anim?.start()
        } else {

            val anim = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewAnimationUtils.createCircularReveal(filters, cx, cy, radius.toFloat(), 0f)
            } else {

                filters.visibility = View.INVISIBLE
                null
            }

            anim?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    filters.visibility = View.INVISIBLE
//                    toggleFAB(onClick = false)
                }
            })

            anim?.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(Gravity.START)) {
            drawer_layout.closeDrawers()
        } else if (filters.visibility == View.VISIBLE) {
            toggleFilters()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                navController.navigate(R.id.nav_search)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.nav_cons) {
            val con = database.getConferences().firstOrNull { it.conference.index == item.itemId }
            if (con != null) database.changeConference(con)
        } else {
            val current = navController.currentDestination.id
            if (item.itemId != current) {
                navController.navigate(item.itemId)
            }
        }

        drawer_layout.closeDrawers()
        return true
    }
}

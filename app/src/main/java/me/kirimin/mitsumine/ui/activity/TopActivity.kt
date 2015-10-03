package me.kirimin.mitsumine.ui.activity

import me.kirimin.mitsumine.db.AccountDAO
import me.kirimin.mitsumine.db.FeedDAO
import me.kirimin.mitsumine.db.KeywordDAO
import me.kirimin.mitsumine.db.UserIdDAO
import me.kirimin.mitsumine.network.api.FeedApi.CATEGORY
import me.kirimin.mitsumine.network.api.FeedApi.TYPE
import me.kirimin.mitsumine.ui.activity.search.KeywordSearchActivity
import me.kirimin.mitsumine.ui.activity.search.MyBookmarksActivity
import me.kirimin.mitsumine.ui.activity.search.UserSearchActivity
import me.kirimin.mitsumine.ui.fragment.FeedFragment

import me.kirimin.mitsumine.R

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.ArrayAdapter
import android.widget.TextView

import com.makeramen.RoundedTransformationBuilder
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.activity_top.*
import me.kirimin.mitsumine.ui.activity.search.SearchActivity
import java.io.Serializable

public class TopActivity : AppCompatActivity() {

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mSelectedCategory = CATEGORY.MAIN
    private var mSelectedType = TYPE.HOT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        // 古い既読を削除
        FeedDAO.deleteOldData()

        setSupportActionBar(toolBar)
        val actionBar = supportActionBar

        actionBar.navigationMode = ActionBar.NAVIGATION_MODE_LIST
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        val data = arrayOf(getString(R.string.feed_hot), getString(R.string.feed_new))
        val adapter = ArrayAdapter(actionBar.themedContext, android.R.layout.simple_list_item_1, data)
        actionBar.setListNavigationCallbacks(adapter, { position, id ->
            drawerLayout.closeDrawers()
            mSelectedType = if (position == 0) TYPE.HOT else TYPE.NEW
            refreshShowCategoryAndType()
            true
        })

        mDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name)
        drawerLayout.setDrawerListener(mDrawerToggle)
        toolBar.setOnClickListener {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawer(Gravity.LEFT)
            } else {
                drawerLayout.openDrawer(Gravity.LEFT)
            }
        }

        navigationReadTextView.setOnClickListener {
            startActivity(Intent(this, ReadActivity::class.java))
            drawerLayout.closeDrawers()
        }
        navigationSettingsTextView.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            drawerLayout.closeDrawers()
        }
        navigationKeywordSearchTextView.setOnClickListener {
            startActivity(Intent(this, KeywordSearchActivity::class.java))
            drawerLayout.closeDrawers()
        }
        navigationUserSearchTextView.setOnClickListener {
            startActivity(Intent(this, UserSearchActivity::class.java))
            drawerLayout.closeDrawers()
        }
        navigationUserInfoLayout.setOnClickListener {
            val intent = Intent(this, MyBookmarksActivity::class.java)
            intent.putExtras(SearchActivity.buildBundle("", false))
            startActivity(intent)
        }
        navigationLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.MAIN))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.SOCIAL))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.ECONOMICS))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.LIFE))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.KNOWLEDGE))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.IT))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.FUN))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.ENTERTAINMENT))
        navigationCategories.addView(makeNavigationCategoryButton(CATEGORY.GAME))

        savedInstanceState?.let {
            mSelectedCategory = savedInstanceState.getSerializable(CATEGORY::class.java.canonicalName) as CATEGORY
            mSelectedType = savedInstanceState.getSerializable(TYPE::class.java.canonicalName) as TYPE
        }
        refreshShowCategoryAndType()
    }

    override fun onStart() {
        super.onStart()
        loadNavigationButtons()
        val account = AccountDAO.get()
        if (account != null) {
            navigationUserInfoLayout.visibility = View.VISIBLE
            navigationLoginButton.visibility = View.GONE
            navigationUserName.text = account.urlName
            val transformation = RoundedTransformationBuilder().borderWidthDp(0f).cornerRadiusDp(48f).oval(false).build()
            Picasso.with(this).load(account.imageUrl).transform(transformation).fit().into(navigationUserIconImageView)
        } else {
            navigationUserInfoLayout.visibility = View.GONE
            navigationLoginButton.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CATEGORY::class.java.canonicalName, mSelectedCategory as Serializable)
        outState.putSerializable(TYPE::class.java.canonicalName, mSelectedType as Serializable)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val unRead = menu!!.add(Menu.NONE, 1, Menu.NONE, R.string.type_read_later)
        unRead.setIcon(R.drawable.ic_action_labels)
        MenuItemCompat.setShowAsAction(unRead, MenuItemCompat.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        if (item!!.title == getString(R.string.type_read_later)) {
            startActivity(Intent(this, ReadLaterActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT)
        } else {
            super.onBackPressed()
        }
    }

    private fun loadNavigationButtons() {
        navigationAdditions.removeAllViews()
        KeywordDAO.findAll().forEach { keyword ->
            navigationAdditions.addView(makeNavigationButton(keyword,
                    OnClickListener {
                        val intent = Intent(this@TopActivity, KeywordSearchActivity::class.java)
                        startActivity(intent.putExtras(SearchActivity.buildBundle(keyword)))
                        drawerLayout.closeDrawers()
                    },
                    OnLongClickListener { v ->
                        buildDeleteDialog(keyword, v).show()
                        false
                    }))
        }
        UserIdDAO.findAll().forEach { userId ->
            navigationAdditions.addView(makeNavigationButton(userId,
                    OnClickListener {
                        val intent = Intent(this@TopActivity, UserSearchActivity::class.java)
                        startActivity(intent.putExtras(SearchActivity.buildBundle(userId)))
                        drawerLayout.closeDrawers()
                    },
                    OnLongClickListener { v ->
                        buildDeleteUserIdDialog(userId, v).show()
                        false
                    }))
        }
    }

    private fun makeNavigationCategoryButton(category: CATEGORY): View {
        return makeNavigationButton(getString(category.labelResource), OnClickListener { v ->
            drawerLayout.closeDrawers()
            mSelectedCategory = category
            refreshShowCategoryAndType()
        }, null)
    }

    private fun makeNavigationButton(label: String, onClick: OnClickListener, onLongClick: OnLongClickListener?): View {
        val navigationView = LayoutInflater.from(applicationContext).inflate(R.layout.activity_top_navigation, null)
        val textView = navigationView.findViewById(R.id.MainNavigationTextView) as TextView
        textView.setText(label)
        textView.setOnClickListener(onClick)
        textView.setOnLongClickListener(onLongClick)
        return navigationView
    }

    private fun refreshShowCategoryAndType() {
        supportActionBar.setTitle(mSelectedCategory.labelResource)
        supportActionBar.setSelectedNavigationItem(if (mSelectedType == TYPE.HOT) 0 else 1)
        supportFragmentManager.beginTransaction().replace(R.id.containerFrameLayout, FeedFragment.newFragment(mSelectedCategory, mSelectedType)).commit()
        for (i in 0..navigationCategories.childCount - 1) {
            val categoryButton = navigationCategories.getChildAt(i).findViewById(R.id.MainNavigationTextView) as TextView
            val isSelectedCategory = categoryButton.text == getString(mSelectedCategory.labelResource)
            categoryButton.setTextColor(ContextCompat.getColor(this, if (isSelectedCategory) R.color.orange else R.color.text))
        }
    }

    private fun buildDeleteDialog(word: String, view: View): AlertDialog {
        return AlertDialog.Builder(this)
                .setTitle(R.string.settings_ngword_delete)
                .setPositiveButton(android.R.string.ok, { dialog, id ->
                    KeywordDAO.delete(word)
                    view.visibility = View.GONE
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    private fun buildDeleteUserIdDialog(word: String, view: View): AlertDialog {
        return AlertDialog.Builder(this)
                .setTitle(R.string.settings_ngword_delete)
                .setPositiveButton(android.R.string.ok, { dialog, id ->
                    UserIdDAO.delete(word)
                    view.visibility = View.GONE
                })
                .setNegativeButton(android.R.string.cancel, null).create()
    }
}

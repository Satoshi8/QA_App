package jp.techacademy.satoshi.takae.qa_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64
import com.google.firebase.database.DatabaseError
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var mToolbar: Toolbar
    private var mGenre = 0



    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList:ArrayList<Question>
    private lateinit var mAdapter:QuestionListAdapter


    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {
        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildAdded(dataSnapshot:DataSnapshot, s: String?){
            val map = dataSnapshot.value as Map<String,String>
            val title = map["title"] ?:""
            val body = map["body"] ?:""
            val name = map["name"] ?:""
            val uid = map["uid"] ?:""
            val imageString = map["image"] ?:""
            val bytes =
                if (imageString.isNotEmpty()){
                    Base64.decode(imageString,Base64.DEFAULT)
                }else {
                    byteArrayOf()
                }
            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String,String>?
            if(answerMap != null){
                for (key in answerMap.keys){
                    val temp = answerMap[key] as Map<String,String>
                    val answerBody = temp["body"] ?:""
                    val answerName = temp["name"] ?:""
                    val answerUid = temp["uid"] ?:""
                    val answer = Answer(answerBody,answerBody,answerName,answerUid,key)
                    answerArrayList.add(answer)
                }
            }
            val question = Question(title,body,name,uid,dataSnapshot.key ?:"",
                mGenre,bytes,answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }
        override fun onChanged(dataSnapshot:DataSnapshot,s:String?){
            val map = dataSnapshot.value as Map<String,String>


            for (question in mQuestionArrayList){
                if(dataSnapshot.key.equals(question.questionUid)){

                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String,String>?
                    if (answerMap != null){
                        for (key in answerMap.keys){
                            val temp = answerMap[key] as Map<String,String>
                            val answerBody = temp["body"] ?:""
                            val answerName = temp["name"] ?:""
                            val answerUid = temp["uid"] ?:""
                            val answer = Answer(answerBody,answerName,answerUid,key)
                            question.answers.add(answer)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
        override fun onChildRemoved(p0: DataSnapshot){
        }
        override fun onChildMoved(p0:DataSnapshot,p1:String?){
        }
        override fun onCancelled(p0: DatabaseError){
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        val fab = findViewById< FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->

         if (mGenre == 0){
             Snackbar.make(view,"ジャンルを選択して下さい",Snackbar.LENGTH_LONG).show()
         }else{

         }
          val user = FirebaseAuth.getInstance().currentUser
            if (user == null){
                val intent = Intent(applicationContext,LoginActivity::class.java)
                startActivity(intent)
            }else {
                val intent = Intent(applicationContext,QuestionSendActivity::class.java)
                intent.putExtra("genre",mGenre)
                startActivity(intent)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this,drawer,mToolbar,R.string.app_name,R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mListView = findViewById(R.id.listView)
        mAdapter = QuestionListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener{parent,view,position,id ->

            val intent = Intent(applicationContext,QuestionDetailActivity::class.java)
            intent.putExtra("question",mQuestionArrayList[position])
            startActivity(intent)

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
      val id = item.itemId
        if (id == R.id.action_settings){
            val intent = Intent(applicationContext,SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem):Boolean{
        val id = item.itemId

        if (id == R.id.nav_hobby){
            mToolbar.title = "趣味"
            mGenre = 1
        }else if (id == R.id.nav_life){
            mToolbar.title = "生活"
            mGenre = 2
        }else if (id == R.id.nav_health){
            mToolbar.title = "健康"
            mGenre = 3
        }else if (id == R.id.nav_computer){
            mToolbar.title = "コンピューター"
            mGenre = 4
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        if (mGenre != null){
            mGenre!!.removeEventListener(mEventListener)
        }
        mGenre = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
        return true

    }
}

private fun Int.removeEventListener(mEventListener: ChildEventListener) {


}

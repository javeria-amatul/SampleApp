package com.branch.sampleapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.branch.sampleapp.Constant
import com.branch.sampleapp.R
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.branch.referral.validators.IntegrationValidator
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {


    private var ivShare: ImageView? = null
    private var etName: EditText? = null
    private var referrer = ""
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ivShare = findViewById(R.id.iv_share)
        etName = findViewById(R.id.et_name)
        setListeners()
    }

    private fun setListeners() {
        ivShare?.setOnClickListener(this)
        etName?.addTextChangedListener(this)
    }


    override fun onStart() {
        super.onStart()
        //Initialize Branch.io
        val branch = Branch.getInstance()
        branch.setRetryCount(5)
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener)
            .withData(if (intent != null) intent.data else null).init()
        IntegrationValidator.validate(this)
    }

    private val branchReferralInitListener = Branch.BranchUniversalReferralInitListener { branchUniversalObject, linkProperties, error ->

        if (error == null) {
            // latest
            val sessionParams = Branch.getInstance().latestReferringParams
            // first
            val installParams = Branch.getInstance().firstReferringParams
            //branchUniversalObject?.contentMetadata?.customMetadata?.get("+is_first_session")
            //sessionParams["+clicked_branch_link"]
            //sessionParams["+is_first_session"]
            if (branchUniversalObject?.contentMetadata?.customMetadata?.containsKey(Constant.USER_NAME) != null) {
                val username = branchUniversalObject?.contentMetadata?.customMetadata?.get(Constant.USER_NAME)
                Toast.makeText(this, "Deep Link User name " + username, Toast.LENGTH_SHORT).show()
                if (username != null && username.length > 0) {
                    showDialogBox(this, username)
                }
            }
        } else {
            Log.e(TAG, error.message)
        }
    }

    private val branchLinkCreateListener = Branch.BranchLinkCreateListener{ url, error ->
        if (error == null) {
            Log.v(TAG, "got my Branch link to share: " + url)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = Constant.TYPE
            intent.putExtra(Intent.EXTRA_TEXT, url)
            startActivity(Intent.createChooser(intent, Constant.SHARE_URL))
        }
    }

    private fun createBranchLink() {

        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(UUID.randomUUID().toString())
            .setTitle(Constant.LINK_TITLE)
            .setContentDescription(Constant.LINK_DESC)
            .setContentImageUrl(Constant.IMG_URL)
            .setContentMetadata(ContentMetadata().addCustomMetadata(Constant.USER_NAME, referrer))

        val linkProperties = LinkProperties()
            .addControlParameter("\$desktop_url", Constant.DEFAULT_URL)
            .addControlParameter("\$ios_url", Constant.DEFAULT_URL)
            .addControlParameter("\$android_url",Constant.DEFAULT_URL)
        showShareView(linkProperties, branchUniversalObject)

        branchUniversalObject.generateShortUrl(this, linkProperties,branchLinkCreateListener)
    }


    private fun showShareView(linkProperties: LinkProperties, branchUniversalObject: BranchUniversalObject) {
        branchUniversalObject.generateShortUrl(this, linkProperties,{ url, error ->
            if (error == null) {
                Log.v(TAG, "got my Branch link to share: " + url)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = Constant.TYPE
                intent.putExtra(Intent.EXTRA_TEXT, url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(Intent.createChooser(intent, "Share app!"))
            } else {
                Log.e(TAG, "onLinkCreate: Branch error: " + error.message)
            }
        })
    }


    override fun onClick(v: View?) {
       when(v?.id) {
           R.id.iv_share -> {
               createBranchLink()
           }
       }
    }

    override fun afterTextChanged(editable: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s?.length != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                referrer = etName?.text.toString()
            }
    }

    fun showDialogBox(activity: Activity, referrer: String) {
        val alertDialog = AlertDialog.Builder(activity)
        val inflater = getLayoutInflater()
        val dialoglayout = inflater.inflate(R.layout.referral_popup, null)
        alertDialog.setView(dialoglayout)
        alertDialog.create()
        val tvReferrer : TextView? = dialoglayout.findViewById(R.id.tv_referrer)
        tvReferrer?.text = referrer
        alertDialog.setPositiveButton(
            "Okay", { dialogInterface, i ->
                dialogInterface.dismiss()
            })
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
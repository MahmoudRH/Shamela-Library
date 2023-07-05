package com.folioreader.ui.adapter
//
//import android.os.Bundle
//import android.util.Log
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentStatePagerAdapter
//import com.folioreader.ui.fragment.FolioPageFragment.Companion.newInstance
//import org.readium.r2.shared.Link
//
///**
// * @author mahavir on 4/2/16.
// */
//class FolioPageFragmentAdapter(
//    fragmentManager: FragmentManager?, private val mSpineReferences: List<Link>,
//    private val mEpubFileName: String, private val mBookId: String
//) : FragmentStatePagerAdapter(fragmentManager!!) {
//    val fragments: ArrayList<Fragment?> = ArrayList(listOf(*arrayOfNulls(mSpineReferences.size)))
//    var savedStateList: ArrayList<Fragment.SavedState>? = null
//        get() {
//            if (field == null) {
//                try {
//                    val field =
//                        FragmentStatePagerAdapter::class.java.getDeclaredField("mSavedState")
//                    field.isAccessible = true
////                    field = field[this] as ArrayList<Fragment.SavedState>
//                } catch (e: Exception) {
//                    Log.e(LOG_TAG, "-> ", e)
//                }
//            }
//            return field
//        }
//        private set
//
//    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
//        fragments[position] = null
//    }
//
//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val fragment = super.instantiateItem(container, position) as Fragment
//        fragments[position] = fragment
//        return fragment
//    }
//
//    override fun getItem(position: Int): Fragment {
//        if (mSpineReferences.isEmpty() || position < 0 || position >= mSpineReferences.size) return fragments[0]!!
//        var fragment = fragments[position]
//        if (fragment == null) {
//            fragment = newInstance(
//                position,
//                mEpubFileName, mSpineReferences[position], mBookId
//            )
//            fragments[position] = fragment
//        }
//        return fragment
//    }
//
//    override fun getCount(): Int {
//        return mSpineReferences.size
//    }
//
//    companion object {
//        private val LOG_TAG = FolioPageFragmentAdapter::class.java.simpleName
//        fun getBundleFromSavedState(savedState: Fragment.SavedState?): Bundle? {
//            var bundle: Bundle? = null
//            try {
//                val field = Fragment.SavedState::class.java.getDeclaredField("mState")
//                field.isAccessible = true
//                bundle = field[savedState] as Bundle
//            } catch (e: Exception) {
//                Log.v(LOG_TAG, "-> $e")
//            }
//            return bundle
//        }
//    }
//}
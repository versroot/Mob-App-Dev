package dk.itu.moapd.x9.gicu

import com.google.android.material.color.DynamicColors
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.x9.gicu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            openFragment(Homepage())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    openFragment(Homepage())
                    true }
                R.id.item_2 -> {
                    openFragment(Latestpage())
                    true }
                R.id.item_3 -> {
                    openFragment(Logpage())
                    true }
                else -> false
            }
        }
    }

}

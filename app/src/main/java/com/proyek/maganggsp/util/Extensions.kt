fun Intent.extractPpid(): String {
    return getStringExtra(NavigationConstants.ARG_PPID)
        ?: getBundleExtra("android:support:navigation:fragment:args")?.getString(NavigationConstants.ARG_PPID)
        ?: getStringExtra("ppid")
        ?: ""
}
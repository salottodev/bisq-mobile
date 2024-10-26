package network.bisq.mobile.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import network.bisq.mobile.AppPresenter
import network.bisq.mobile.domain.data.repository.GreetingRepository

/**
 * Main Presenter as an example of implementation for now.
 */
class MainPresenter(private val greetingRepository: GreetingRepository) : BasePresenter(), AppPresenter {
    // Observable state
    private val _isContentVisible = MutableStateFlow(false)
    override val isContentVisible: StateFlow<Boolean> = _isContentVisible

    private val _greetingText = MutableStateFlow("Welcome!")
    override val greetingText: StateFlow<String> = _greetingText

    fun refresh() {
        _greetingText.value = greetingRepository.getValue()
    }

    // Toggle action
    override fun toggleContentVisibility() {
        _isContentVisible.value = !_isContentVisible.value
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}
package com.rob729.datepicker

class SingleSelectionMode : DefaultSelectionMode() {

    override fun getSelectionStateAfterSelecting(lastSelectionState: SelectionState, dayToSelect: MaterialDayPicker.Weekday): SelectionState {
        return SelectionState.withSingleDay(dayToSelect)
    }

    companion object {
        /**
         * Creates a new instance of a [SingleSelectionMode]
         */
        @JvmStatic
        fun create(): SelectionMode {
            return SingleSelectionMode()
        }
    }
}
package com.rob729.datepicker

interface SelectionMode {

    /**
     * Takes the last [SelectionState] and transforms it to the next
     * [SelectionState] based on the day the user just selected
     *
     * @param lastSelectionState the last [SelectionState]
     * @param dayToSelect the [MaterialDayPicker.Weekday] that was just selected
     * @return An updated [SelectionState] taking into account the day selected
     */
    fun getSelectionStateAfterSelecting(
        lastSelectionState: SelectionState,
        dayToSelect: MaterialDayPicker.Weekday
    ): SelectionState

    /**
     * Takes the last [SelectionState] and transforms it to the next
     * [SelectionState] based on the day the user just deselected
     *
     * @param lastSelectionState the last [SelectionState]
     * @param dayToSelect the [MaterialDayPicker.Weekday] that was just deselected
     * @return An updated [SelectionState] taking into account the day deselected
     */
    fun getSelectionStateAfterDeselecting(
        lastSelectionState: SelectionState,
        dayToDeselect: MaterialDayPicker.Weekday
    ): SelectionState
}
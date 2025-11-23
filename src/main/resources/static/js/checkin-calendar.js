document.addEventListener('DOMContentLoaded', function() {
    const calendarDiv = document.getElementById('checkInCalendar');
    if (!calendarDiv || !window.questCalendarData) return;
    
    const questData = window.questCalendarData;
    if (!questData.startDate || !questData.endDate) return;
    
    const checkIns = questData.checkIns || [];
    const startDateObj = questData.startDate;
    const endDateObj = questData.endDate;
    
    const start = new Date(startDateObj.year, startDateObj.monthValue - 1, startDateObj.dayOfMonth);
    const end = new Date(endDateObj.year, endDateObj.monthValue - 1, endDateObj.dayOfMonth);
    
    const checkedInDates = new Set();
    if (checkIns && checkIns.length > 0) {
        checkIns.forEach(checkIn => {
            if (checkIn.checkInDate) {
                const date = new Date(checkIn.checkInDate.year, checkIn.checkInDate.monthValue - 1, checkIn.checkInDate.dayOfMonth);
                checkedInDates.add(date.toDateString());
            }
        });
    }
    
    let html = '<div class="calendar-container"><div class="calendar-grid">';
    
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    let current = new Date(start);
    let weekCount = 0;
    
    html += '<div class="calendar-weekdays">';
    const weekdays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    weekdays.forEach(day => {
        html += `<div class="weekday-header">${day}</div>`;
    });
    html += '</div>';
    
    html += '<div class="calendar-days-grid">';
    
    const firstDayOfWeek = (current.getDay() + 6) % 7;
    for (let i = 0; i < firstDayOfWeek; i++) {
        html += '<div class="calendar-day empty"></div>';
    }
    
    while (current <= end) {
        const dateStr = current.toDateString();
        const isCheckedIn = checkedInDates.has(dateStr);
        const isToday = dateStr === today.toDateString();
        const isPast = current < today;
        
        let classes = 'calendar-day';
        if (isCheckedIn) classes += ' checked-in';
        if (isToday) classes += ' today';
        if (isPast && !isCheckedIn) classes += ' missed';
        
        html += `<div class="${classes}" title="${formatDate(current)}">`;
        html += `<div class="day-number">${current.getDate()}</div>`;
        if (isCheckedIn) {
            html += '<div class="check-mark">✓</div>';
        }
        html += '</div>';
        
        current.setDate(current.getDate() + 1);
    }
    
    html += '</div></div>';
    html += '<div class="calendar-legend">';
    html += '<span class="legend-item"><span class="legend-box checked-in"></span> Checked In</span>';
    html += '<span class="legend-item"><span class="legend-box today"></span> Today</span>';
    html += '<span class="legend-item"><span class="legend-box missed"></span> Missed</span>';
    html += '</div>';
    
    calendarDiv.innerHTML = html;
});

function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}


const tableBody = document.getElementById("timeTableBody");
const selectedTimeCount = document.getElementById("selectedTimeCount");

const days = ["월", "화", "수", "목", "금", "토", "일"];

for (let hour = 0; hour < 24; hour++) {
    const row = document.createElement("tr");

    const timeHeading = document.createElement("th");
    timeHeading.scope = "row";
    timeHeading.textContent = `${String(hour).padStart(2, "0")}:00`;
    row.appendChild(timeHeading);

    days.forEach((day, dayIndex) => {
        const cell = document.createElement("td");
        const label = document.createElement("label");
        label.className = "time-slot";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.name = "availableSlots";

        // 월요일 0~23, 화요일 24~47, ... 일요일 144~167
        checkbox.value = String(dayIndex * 24 + hour);

        checkbox.setAttribute(
            "aria-label",
            `${day}요일 ${hour}시부터 ${hour + 1}시까지`
        );

        const mark = document.createElement("span");
        mark.className = "time-slot-mark";
        mark.setAttribute("aria-hidden", "true");

        label.append(checkbox, mark);
        cell.appendChild(label);
        row.appendChild(cell);
    });

    tableBody.appendChild(row);
}

tableBody.addEventListener("change", () => {
    const count = tableBody.querySelectorAll(
        'input[name="availableSlots"]:checked'
    ).length;

    selectedTimeCount.textContent = `선택한 시간: 주 ${count}시간`;
});
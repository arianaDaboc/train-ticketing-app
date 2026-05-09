# Train Ticketing App
## Implemented features
- predefined routes and trains
- booking one or multiple tickets
- overbooking prevention
- booking confirmation email message
- finding departure/arrival times between two stations
- direct routes and one-change routes
- fastest route highlight with total duration
- clear error when no connection exists
- admin add/remove/modify routes
- admin add/remove/modify trains
- admin view bookings for any train
- admin register delay and notify booked customers
## Example inputs and outputs
### 1) Book tickets
Input flow:
- choose `1`
- train: `T1`
- from: `Bucharest`
- to: `Brasov`
- tickets: `2`
- email: `ana@mail.com`

Output:
`Booking confirmed.`
`Email sent to ana@mail.com:`
`Your booking is confirmed for train T1, Bucharest -> Brasov, tickets: 2.`
If capacity is full:
`Booking rejected. Not enough free seats. Available: X`

### 2) Find times between stations
Input flow:
- choose `2`
- departure: `Bucharest`
- arrival: `Cluj`

Output example:
`Possible routes:`
`- Direct: Train T1 | Bucharest 08:00 -> Cluj 12:00`
`Best option: Direct: Train T1 | Bucharest 08:00 -> Cluj 12:00 | duration 4h`

Input flow:
- choose `2`
- departure: `Constanta`
- arrival: `Cluj`
Output example (changeover):
`Possible routes:`
`- Changeover: Train T2 Constanta 07:30 -> Bucharest 10:00 | Train T1 Bucharest 08:00 -> Cluj 12:00`
If no route exists:
`No possible link between these stations.`

### 3) Admin route management
Open admin menu:
- choose `3`
Add route:
- choose `1`
- route id: `R4`
- stations: `Arad, Deva, Sibiu`

Output:
`Route added.`

Modify route:
- choose `3`
- route id: `R4`
- stations: `Arad, Alba Iulia, Sibiu`

Output:
`Route modified. Linked trains were updated with default times and cleared bookings.`

Remove route:
- choose `2`
- route id: `R4`

Output:
`Route removed.`

### 4) Admin train management

Add train:
- choose admin `4`
- train id: `T4`
- route id: `R1`
- capacity: `90`
- times: `08:10`, `09:00`, `10:00`, `11:00`, `12:10`

Output:
`Train added.`

Modify train:
- choose admin `6`
- train id: `T4`
- new capacity: `100`
- new times for all route stations

Output:
`Train modified.`

Remove train:
- choose admin `5`
- train id: `T4`

Output:
`Train removed.`

### 5) Show bookings for a train

- choose admin `7`
- train id: `T1`

Output example:
`Bookings for train T1:`
`- ana@mail.com | Bucharest -> Brasov | tickets: 2 | at: 2026-05-09 16:30`

### 6) Register delay and notify customers
- choose admin `8`
- train id: `T1`
- delay minutes: `25`

Output example:
`Notifications:`
`Email to ana@mail.com: Train T1 has a delay of 25 minutes.`
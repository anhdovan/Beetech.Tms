# 📑 Textile Management System – Use Cases
---
UC‑01:  
Actor: Textile Staff  
Boundary: Mobile RFID Reader (Android)  
Description: Staff record circulation transactions of textile items at any stage (department, distribution center, laundry, warehouse) using handheld RFID readers.  
Diagram:  
```mermaid
usecaseDiagram
actor TextileStaff as TS
rectangle "Mobile RFID Reader (Android)" {
  TS --> (Outbound Dirty)
  TS --> (Inbound Dirty)
  TS --> (Outbound Clean)
  TS --> (Inbound Clean)
  TS --> (Issue Clean)
  TS --> (Receive Clean)
}
```

---

UC‑02:  
Actor: Textile Staff  
Boundary: Mobile RFID Reader (Android)  
Description: Staff perform ad‑hoc inventory audits at any location to reconcile actual textile counts with system records.  
Diagram:  
```mermaid
usecaseDiagram
actor TextileStaff as TS
rectangle "Mobile RFID Reader (Android)" {
  TS --> (Inventory Audit)
}
```

---

UC‑03:  
Actor: Textile Staff  
Boundary: Mobile RFID Reader (Android)  
Description: Staff record transfers of textiles between departments, hotel floors, or factory units using handheld RFID readers.  
Diagram:  
```mermaid
usecaseDiagram
actor TextileStaff as TS
rectangle "Mobile RFID Reader (Android)" {
  TS --> (Internal Transfer)
}
```

---

UC‑04:  
Actor: Textile Staff, Supervisor  
Boundary: Web Portal (Blazor)  
Description: Staff manually record transfers between units via the Web Dashboard when RFID readers are unavailable.  
Diagram:  
```mermaid
usecaseDiagram
actor TextileStaff as TS
actor Supervisor as SV
rectangle "Web Portal (Blazor)" {
  TS --> (Manual Internal Transfer)
  SV --> (Manual Internal Transfer)
}
```

---

UC‑05:  
Actor: Manager, Supervisor  
Boundary: Web Portal (Blazor)  
Description: Managers view comprehensive reports and traceability of textiles across all locations.  
Diagram:  
```mermaid
usecaseDiagram
actor Manager as M
actor Supervisor as SV
rectangle "Web Portal (Blazor)" {
  M --> (View Inventory Reports)
  M --> (Trace RFID History)
  SV --> (Export Reports)
}
```

---

UC‑06:  
Actor: Textile Staff  
Boundary: Fixed RFID Reader (Avalonia Desktop)  
Description: Staff perform bulk scanning operations (inbound, outbound, inventory) using fixed RFID readers connected to the desktop client.  
Diagram:  
```mermaid
usecaseDiagram
actor TextileStaff as TS
rectangle "Fixed RFID Reader (Avalonia Desktop)" {
  TS --> (Bulk Scan Inbound)
  TS --> (Bulk Scan Outbound)
  TS --> (Bulk Inventory Count)
}
```

---

UC‑07:  
Actor: Textile Staff, Supervisor, Manager  
Boundary: Web Portal (Blazor), Mobile RFID Reader (Android), Fixed RFID Reader (Avalonia Desktop)  
Description: Combined overview of all actors and boundaries in the Textile Management System.  
Diagram:  
```mermaid
usecaseDiagram
actor TextileStaff as TS
actor Supervisor as SV
actor Manager as M

rectangle "Mobile RFID Reader (Android)" {
  TS --> (Outbound Dirty)
  TS --> (Inbound Dirty)
  TS --> (Outbound Clean)
  TS --> (Inbound Clean)
  TS --> (Issue Clean)
  TS --> (Receive Clean)
  TS --> (Inventory Audit)
  TS --> (Internal Transfer)
}

rectangle "Web Portal (Blazor)" {
  TS --> (Manual Internal Transfer)
  SV --> (Manual Internal Transfer)
  M --> (View Inventory Reports)
  M --> (Trace RFID History)
  SV --> (Export Reports)
}

rectangle "Fixed RFID Reader (Avalonia Desktop)" {
  TS --> (Bulk Scan Inbound)
  TS --> (Bulk Scan Outbound)
  TS --> (Bulk Inventory Count)
}
```

---

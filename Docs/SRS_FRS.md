📑 Software Requirements Specification (SRS)

1\. Introduction

1.1 Purpose

The Textile Management System (TMS) provides hospitals, hotels, and industrial laundries with a centralized RFID-based solution to manage textile items. It ensures real-time visibility, lifecycle tracking, and secure access across multiple platforms.

1.2 Scope

• 	Manage textile items with RFID tags.

• 	Support inbound/outbound transactions.

• 	Provide inventory visibility and lifecycle tracking.

• 	Enable role-based access for different stakeholders.

• 	Multi-platform clients: Blazor web, Avalonia desktop, Android mobile.

• 	Central SQL Server database with secure authentication/authorization.

1.3 Stakeholders

• 	Administrators: Manage system configuration, roles, and reporting.

• 	Laundry Staff: Handle inbound/outbound textile operations.

• 	Hotel Housekeeping: Track linen availability and usage.

• 	Hospital Inventory Managers: Monitor textile lifecycle and compliance.



2\. System Overview

• 	Web Portal (Blazor): Admin and customer dashboards.

• 	Desktop Client (Avalonia): Connects to fixed RFID readers for bulk operations.

• 	Mobile Client (Android): Runs on handheld RFID readers for field operations.

• 	Backend Services: REST APIs, authentication, reporting.

• 	Database (SQL Server): Centralized storage for items, lifecycle events, users, roles.



3\. System Features

• 	RFID tag registration and encoding.

• 	Inbound/outbound transaction logging.

• 	Real-time inventory management.

• 	Lifecycle tracking (wash cycles, condemn/reuse).

• 	Reporting dashboards and analytics.

• 	Central authentication/authorization with OAuth2/OpenID Connect.

• 	Logging and auditing of all transactions.



4\. Non-Functional Requirements

• 	Performance: Bulk RFID reads within 2 seconds.

• 	Scalability: Support thousands of textile items across multiple sites.

• 	Security: Role-based access, encrypted communication, audit logs.

• 	Availability: 99.9% uptime with cloud deployment option.

• 	Maintainability: Modular architecture, easy updates.



📑 Functional Requirements Specification (FRS)

1\. Authentication \& Authorization

• 	Users must log in via centralized identity service.

• 	Roles: Admin, Laundry Staff, Hotel Housekeeping, Hospital Inventory Manager.

• 	Token-based authorization for API calls.

2\. Textile Item Management

• 	Register textile items with RFID tags.

• 	Store metadata: item type, lifecycle status, washing cycles, location.

• 	Update item status (available, in use, soiled, condemned).

3\. Inbound/Outbound Operations

• 	Inbound: Scan textiles received from laundries.

• 	Outbound: Scan textiles dispatched to hotels/hospitals.

• 	Log transactions with timestamps, user, and location.

4\. Inventory Management

• 	Real-time stock visibility at laundry and customer sites.

• 	Bulk scanning via fixed RFID readers.

• 	Mobile scanning via handheld readers.

• 	Alerts for shortages, dormant stock, or misplacements.

5\. Lifecycle Tracking

• 	Track washing cycles per item.

• 	Flag items for condemnation/replacement.

• 	Generate lifecycle reports.

6\. Reporting \& Dashboards

• 	Web dashboards for admins and managers.

• 	Reports: inventory levels, lifecycle status, transaction logs.

• 	Export to Excel/PDF.

7\. Integration

• 	REST APIs for client communication.

• 	ERP integration for invoicing and order management.


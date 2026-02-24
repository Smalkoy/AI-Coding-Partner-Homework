# API Reference

Complete API documentation for the Intelligent Customer Support System.

**Base URL:** `http://localhost:8080`

---

## Table of Contents

1. [Tickets](#tickets)
   - [Create Ticket](#create-ticket)
   - [Get Ticket](#get-ticket)
   - [List Tickets](#list-tickets)
   - [Update Ticket](#update-ticket)
   - [Delete Ticket](#delete-ticket)
2. [Import](#import)
   - [Bulk Import](#bulk-import)
3. [Classification](#classification)
   - [Auto-Classify Ticket](#auto-classify-ticket)
4. [Data Models](#data-models)
5. [Error Responses](#error-responses)

---

## Tickets

### Create Ticket

Creates a new support ticket.

**Endpoint:** `POST /tickets`

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `autoClassify` | boolean | false | Automatically classify the ticket |

**Request Body:**

```json
{
  "customerId": "CUST001",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I am unable to login after changing my password. Getting access denied error.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "assignedTo": "agent1",
  "tags": ["login", "urgent"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120",
    "deviceType": "desktop"
  }
}
```

**Response:** `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST001",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I am unable to login after changing my password. Getting access denied error.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "resolvedAt": null,
  "assignedTo": "agent1",
  "tags": ["login", "urgent"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120",
    "deviceType": "desktop"
  }
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Cannot login to my account",
    "description": "I am unable to login after changing my password. Getting access denied error.",
    "category": "account_access",
    "priority": "high"
  }'
```

**cURL Example with Auto-Classify:**

```bash
curl -X POST "http://localhost:8080/tickets?autoClassify=true" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Cannot login to my account",
    "description": "I am unable to login after changing my password."
  }'
```

---

### Get Ticket

Retrieves a specific ticket by ID.

**Endpoint:** `GET /tickets/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Ticket ID |

**Response:** `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST001",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I am unable to login after changing my password.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "resolvedAt": null,
  "assignedTo": null,
  "tags": [],
  "metadata": null
}
```

**cURL Example:**

```bash
curl http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### List Tickets

Lists all tickets with optional filtering.

**Endpoint:** `GET /tickets`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Filter by category |
| `priority` | string | Filter by priority |
| `status` | string | Filter by status |
| `customerId` | string | Filter by customer ID |

**Response:** `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "customerId": "CUST001",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Cannot login to my account",
    "description": "I am unable to login after changing my password.",
    "category": "account_access",
    "priority": "high",
    "status": "new",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "resolvedAt": null,
    "assignedTo": null,
    "tags": [],
    "metadata": null
  }
]
```

**cURL Examples:**

```bash
# List all tickets
curl http://localhost:8080/tickets

# Filter by category
curl "http://localhost:8080/tickets?category=technical_issue"

# Filter by priority
curl "http://localhost:8080/tickets?priority=urgent"

# Filter by status
curl "http://localhost:8080/tickets?status=new"

# Multiple filters
curl "http://localhost:8080/tickets?category=bug_report&priority=high&status=new"
```

---

### Update Ticket

Updates an existing ticket. Only provided fields are updated.

**Endpoint:** `PUT /tickets/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Ticket ID |

**Request Body:**

```json
{
  "priority": "urgent",
  "status": "in_progress",
  "assignedTo": "agent2"
}
```

**Response:** `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST001",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I am unable to login after changing my password.",
  "category": "account_access",
  "priority": "urgent",
  "status": "in_progress",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:35:00Z",
  "resolvedAt": null,
  "assignedTo": "agent2",
  "tags": [],
  "metadata": null
}
```

**cURL Example:**

```bash
curl -X PUT http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "urgent",
    "status": "in_progress",
    "assignedTo": "agent2"
  }'
```

---

### Delete Ticket

Deletes a ticket.

**Endpoint:** `DELETE /tickets/{id}`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Ticket ID |

**Response:** `204 No Content`

**cURL Example:**

```bash
curl -X DELETE http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

## Import

### Bulk Import

Imports tickets from a CSV, JSON, or XML file.

**Endpoint:** `POST /tickets/import`

**Content-Type:** `multipart/form-data`

**Form Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `file` | file | The file to import (CSV, JSON, or XML) |

**Response:** `200 OK`

```json
{
  "totalRecords": 10,
  "successfulImports": 8,
  "failedImports": 2,
  "errors": [
    {
      "recordIndex": 3,
      "field": "customer_email",
      "message": "customer_email must be a valid email address",
      "rejectedValue": "invalid-email"
    },
    {
      "recordIndex": 7,
      "field": "description",
      "message": "description must be between 10 and 2000 characters",
      "rejectedValue": "short"
    }
  ]
}
```

**cURL Example - CSV:**

```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@tickets.csv"
```

**cURL Example - JSON:**

```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@tickets.json"
```

**cURL Example - XML:**

```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@tickets.xml"
```

### CSV Format

```csv
customer_id,customer_email,customer_name,subject,description,category,priority,status,assigned_to,tags,metadata_source,metadata_browser,metadata_device_type
CUST001,john@example.com,John Doe,Cannot login,Unable to login to account,account_access,high,new,agent1,"login,urgent",web_form,Chrome 120,desktop
```

### JSON Format

```json
[
  {
    "customer_id": "CUST001",
    "customer_email": "john@example.com",
    "customer_name": "John Doe",
    "subject": "Cannot login",
    "description": "Unable to login to my account",
    "category": "account_access",
    "priority": "high",
    "tags": ["login", "urgent"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome 120",
      "device_type": "desktop"
    }
  }
]
```

### XML Format

```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST001</customer_id>
    <customer_email>john@example.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Cannot login</subject>
    <description>Unable to login to my account</description>
    <category>account_access</category>
    <priority>high</priority>
    <tags>
      <tag>login</tag>
      <tag>urgent</tag>
    </tags>
  </ticket>
</tickets>
```

---

## Classification

### Auto-Classify Ticket

Automatically classifies a ticket based on its content.

**Endpoint:** `POST /tickets/{id}/auto-classify`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Ticket ID |

**Response:** `200 OK`

```json
{
  "category": "account_access",
  "priority": "high",
  "confidence": 0.75,
  "reasoning": "Category 'account_access' detected based on keywords: login, password. Priority 'high' detected based on keywords: blocking. Moderate confidence classification.",
  "keywordsFound": ["login", "password", "blocking"]
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000/auto-classify
```

---

## Data Models

### Ticket

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | UUID | Auto | Unique identifier |
| `customerId` | string | Yes | Customer identifier (max 100 chars) |
| `customerEmail` | string | Yes | Valid email address |
| `customerName` | string | Yes | Customer name (max 200 chars) |
| `subject` | string | Yes | Ticket subject (1-200 chars) |
| `description` | string | Yes | Ticket description (10-2000 chars) |
| `category` | enum | No | Ticket category (default: other) |
| `priority` | enum | No | Ticket priority (default: medium) |
| `status` | enum | No | Ticket status (default: new) |
| `createdAt` | datetime | Auto | Creation timestamp |
| `updatedAt` | datetime | Auto | Last update timestamp |
| `resolvedAt` | datetime | No | Resolution timestamp |
| `assignedTo` | string | No | Assigned agent (max 100 chars) |
| `tags` | array | No | List of tags |
| `metadata` | object | No | Additional metadata |

### Enums

**Category:**
- `account_access` - Login, password, authentication issues
- `technical_issue` - Bugs, errors, system problems
- `billing_question` - Payments, invoices, refunds
- `feature_request` - Enhancements, suggestions
- `bug_report` - Defects with reproduction steps
- `other` - Uncategorizable issues

**Priority:**
- `urgent` - Immediate attention required
- `high` - Important, needs quick resolution
- `medium` - Standard priority (default)
- `low` - Can be addressed when time permits

**Status:**
- `new` - Just created, not yet triaged
- `in_progress` - Being worked on
- `waiting_customer` - Awaiting customer response
- `resolved` - Solution provided
- `closed` - Ticket completed

**Source:**
- `web_form` - Submitted via website
- `email` - Received via email
- `api` - Created via API
- `chat` - From live chat
- `phone` - From phone call

**Device Type:**
- `desktop` - Desktop/laptop computer
- `mobile` - Mobile phone
- `tablet` - Tablet device

---

## Error Responses

### Validation Error (400)

```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/tickets",
  "details": [
    {
      "field": "customerEmail",
      "message": "customer_email must be a valid email address",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

### Not Found (404)

```json
{
  "error": "Not Found",
  "message": "Ticket not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/tickets/550e8400-e29b-41d4-a716-446655440000",
  "details": null
}
```

### Unsupported Media Type (415)

```json
{
  "error": "Unsupported Media Type",
  "message": "Unsupported file format: text/plain",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/tickets/import",
  "details": null
}
```

### Internal Server Error (500)

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/tickets",
  "details": null
}
```

---

## HTTP Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created |
| 204 | No Content | Successful deletion |
| 400 | Bad Request | Validation or parse error |
| 404 | Not Found | Resource not found |
| 415 | Unsupported Media Type | Invalid file format |
| 500 | Internal Server Error | Server error |

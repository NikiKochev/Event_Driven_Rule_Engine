# Sample Kafka Messages for Testing

## Message 1: All Conditions Test (Should Match ALL conditions)
```json
{
  "field1": "A",
  "field2": "C",
  "field3": "exists",
  "numericField": 150,
  "numericField2": 30,
  "numericField3": 15,
  "numericField4": 100,
  "listField": ["item1", "item3"],
  "listField2": ["item4", "item5"]
}
```

## Message 2: Any Conditions Test (Should Match ANY condition - status active)
```json
{
  "status": "active",
  "priority": 3,
  "category": ["normal"],
  "createdAt": "2026-06-18T08:00:00Z"
}
```

## Message 3: Any Conditions Test (Should Match ANY condition - high priority)
```json
{
  "status": "pending",
  "priority": 8,
  "category": ["standard"],
  "createdAt": "2026-06-18T09:30:00Z"
}
```

## Message 4: Numeric Operations Test
```json
{
  "sourceField": "copiedData",
  "part1": "Hello",
  "part2": "World",
  "part3": "Test",
  "password": "secretPass123"
}
```

## Message 5: Simple Condition Test
```json
{
  "simpleField": "test"
}
```

## Message 6: Complex All Conditions Test
```json
{
  "field1": "X",
  "field2": "Z",
  "field3": "present",
  "field5": 75,
  "field6": 90,
  "field7": ["keyword", "other"]
}
```

## Message 7: Edge Case - Numeric with Zero and Negatives
```json
{
  "numericValue": 100,
  "zeroAdd": 50,
  "negativeSubtract": 200,
  "fractionalMultiply": 100,
  "oneMultiply": 75
}
```

## Message 8: Premium User - Complex Any Scenario
```json
{
  "type": "premium",
  "score": 85,
  "tags": ["standard", "member"],
  "verified": false
}
```

## Message 9: VIP User - Complex Any Scenario
```json
{
  "type": "vip",
  "score": 70,
  "tags": ["member"],
  "verified": true
}
```

## Message 10: High Score User - Complex Any Scenario
```json
{
  "type": "standard",
  "score": 95,
  "tags": ["active"],
  "verified": false
}
```

## Message 11: Featured User - Complex Any Scenario
```json
{
  "type": "regular",
  "score": 60,
  "tags": ["featured", "active"],
  "verified": false
}
```

## Message 12: Verified User - Complex Any Scenario
```json
{
  "type": "basic",
  "score": 50,
  "tags": ["new"],
  "verified": true
}
```

## Message 13: No Match User - Should Not Match Any Scenario
```json
{
  "type": "basic",
  "score": 40,
  "tags": ["new"],
  "verified": false
}
```

## Message 14: Partial ALL Conditions (Missing some fields)
```json
{
  "field1": "A",
  "field2": "C",
  "numericField": 150,
  "numericField2": 30,
  "listField": ["item1"]
}
```

## Message 15: Product Catalog Entry
```json
{
  "product": {
    "id": "PROD-5678",
    "name": "Wireless Headphones",
    "category": "Electronics",
    "price": 149.99,
    "stock": 250,
    "rating": 4.5,
    "reviews": 1234,
    "featured": true,
    "tags": ["wireless", "bluetooth", "audio"]
  }
}
```

## Message 16: Transaction Event
```json
{
  "transaction": {
    "id": "TXN-9876",
    "userId": "USER-456",
    "amount": 299.50,
    "currency": "USD",
    "status": "completed",
    "timestamp": "2026-06-18T14:22:35Z",
    "paymentMethod": "credit_card",
    "merchantId": "MERCH-123"
  }
}
```

## Message 17: Notification Payload
```json
{
  "notification": {
    "type": "alert",
    "priority": 10,
    "userId": "USER-999",
    "title": "System Maintenance",
    "message": "Scheduled maintenance in 1 hour",
    "timestamp": "2026-06-18T15:00:00Z",
    "channels": ["email", "sms", "push"],
    "actionRequired": true
  }
}
```

## Message 18: Analytics Event with Multiple Nested Levels
```json
{
  "analytics": {
    "sessionId": "SESS-ABC123",
    "user": {
      "id": "USER-777",
      "segment": "premium",
      "cohort": "2026-Q2"
    },
    "event": {
      "name": "page_view",
      "page": "/dashboard",
      "duration": 45,
      "interactions": 12
    },
    "device": {
      "type": "desktop",
      "os": "Windows",
      "browser": "Chrome"
    },
    "metrics": {
      "loadTime": 1.2,
      "errorCount": 0,
      "apiCalls": 8
    }
  }
}
```

## Message 19: Inventory Update Event
```json
{
  "inventory": {
    "warehouseId": "WH-001",
    "productId": "PROD-1111",
    "quantity": 500,
    "location": "A-12-5",
    "lastUpdated": "2026-06-18T16:45:00Z",
    "status": "in_stock",
    "reorderLevel": 100,
    "supplier": "SUP-ABC"
  }
}
```

## Message 20: Customer Support Ticket
```json
{
  "ticket": {
    "id": "TICK-5555",
    "customerId": "CUST-888",
    "priority": 7,
    "category": ["technical", "billing"],
    "status": "open",
    "assignedTo": "AGENT-22",
    "createdAt": "2026-06-18T11:15:00Z",
    "lastUpdated": "2026-06-18T13:30:00Z",
    "tags": ["urgent", "vip_customer"]
  }
}
```

## Message 21: IoT Sensor Data
```json
{
  "sensor": {
    "deviceId": "SENSOR-4321",
    "type": "temperature",
    "location": "Building-A-Floor-3",
    "readings": {
      "temperature": 22.5,
      "humidity": 45,
      "pressure": 1013.25
    },
    "timestamp": "2026-06-18T17:00:00Z",
    "status": "active",
    "batteryLevel": 85,
    "alertThreshold": 30
  }
}
```


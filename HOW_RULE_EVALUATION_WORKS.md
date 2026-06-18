# How to Check if a Rule is Correct - Complete Guide

## Your Question:
**JSON Structure:**
```json
{
  "objectOne": {
    "name": {
      "firstName": "A",
      "secondName": "nameSecond"
    }
  },
  "objectTwo": {
    ...
  }
}
```

**Rule:**
```json
{
  "path": "$.firstName",
  "operator": "equals",
  "value": "A"
}
```

**How to check if it's correct?**

---

## Answer: Complete Implementation

### 1. The Code (AllCondition.java)

The `AllCondition` class implements three key methods:

#### a) `execute()` - Main entry point
```java
public void execute(JsonNode context, List<RulesLoader.Rule> rules) {
    for (RulesLoader.Rule rule : rules) {
        if (!evaluateRule(context, rule)) {
            throw new RuntimeException("Rule failed: " + rule);
        }
    }
    // If we reach here, all rules passed
}
```

#### b) `extractValueByPath()` - Navigate JSON
```java
private JsonNode extractValueByPath(JsonNode context, String path) {
    // Remove "$." prefix: "$.firstName" → "firstName"
    String cleanPath = path.startsWith("$.") ? path.substring(2) : path;
    
    // Split by dots: "objectOne.name.firstName" → ["objectOne", "name", "firstName"]
    String[] pathParts = cleanPath.split("\\.");
    
    // Navigate through JSON
    JsonNode current = context;
    for (String part : pathParts) {
        current = current.get(part);
    }
    
    return current;
}
```

#### c) `applyOperator()` - Check the condition
```java
private boolean applyOperator(JsonNode actualValue, String operator, String expectedValue) {
    String actualText = actualValue.asText();
    
    switch (operator.toLowerCase()) {
        case "equals":
            return actualText.equals(expectedValue);
        case "notequals":
            return !actualText.equals(expectedValue);
        case "contains":
            return actualText.contains(expectedValue);
        case "greaterthan":
            return Double.parseDouble(actualText) > Double.parseDouble(expectedValue);
        // ... more operators
    }
}
```

---

## 2. How It Works - Step by Step

### Example 1: Simple Rule ✅
```json
JSON: { "firstName": "A" }
Rule: { "path": "$.firstName", "operator": "equals", "value": "A" }
```

**Steps:**
1. `extractValueByPath()` removes `$.` → looks for `firstName`
2. Finds `"A"` in the JSON
3. `applyOperator()` checks: `"A".equals("A")` → **TRUE** ✅
4. Rule PASSES!

---

### Example 2: Nested Path ✅
```json
JSON: {
  "objectOne": {
    "name": {
      "firstName": "Alice"
    }
  }
}
Rule: { "path": "$.objectOne.name.firstName", "operator": "equals", "value": "Alice" }
```

**Steps:**
1. Remove `$.` → `objectOne.name.firstName`
2. Split by dots → `["objectOne", "name", "firstName"]`
3. Navigate:
   - `context.get("objectOne")` → `{ "name": { "firstName": "Alice" } }`
   - `.get("name")` → `{ "firstName": "Alice" }`
   - `.get("firstName")` → `"Alice"`
4. Compare: `"Alice".equals("Alice")` → **TRUE** ✅
5. Rule PASSES!

---

### Example 3: Rule Fails ❌
```json
JSON: { "firstName": "B" }
Rule: { "path": "$.firstName", "operator": "equals", "value": "A" }
```

**Steps:**
1. Extract value: `"B"`
2. Compare: `"B".equals("A")` → **FALSE** ❌
3. Throws exception: `RuntimeException("Rule failed")`

---

## 3. Supported Operators

| Operator | Example | Description |
|----------|---------|-------------|
| `equals` | `"A" equals "A"` | Exact match |
| `notequals` | `"A" notequals "B"` | Not equal |
| `contains` | `"alice@example.com" contains "@example"` | Substring check |
| `startswith` | `"Alice" startswith "Ali"` | Prefix check |
| `endswith` | `"test.json" endswith ".json"` | Suffix check |
| `greaterthan` | `"25" > "18"` | Numeric comparison |
| `lessthan` | `"15" < "18"` | Numeric comparison |

---

## 4. Why Use JsonNode?

**Problem with Map<String, Object>:**
```java
// Ugly and error-prone
Map<String, Object> objectOne = (Map<String, Object>) context.get("objectOne");
Map<String, Object> name = (Map<String, Object>) objectOne.get("name");
String firstName = (String) name.get("firstName");  // 😰 Too many casts!
```

**Solution with JsonNode:**
```java
// Clean and simple
String firstName = context.get("objectOne")
                         .get("name")
                         .get("firstName")
                         .asText();  // 😊 Much better!

// Or even simpler:
String firstName = context.at("/objectOne/name/firstName").asText();
```

---

## 5. Complete Working Example

```java
// 1. Parse JSON
ObjectMapper mapper = new ObjectMapper();
String json = """
    {
        "firstName": "A",
        "age": 25
    }
    """;
JsonNode context = mapper.readTree(json);

// 2. Create rule
RulesLoader.Rule rule = new RulesLoader.Rule("$.firstName", "equals", "A");

// 3. Evaluate
AllCondition condition = new AllCondition();
try {
    condition.execute(context, List.of(rule));
    System.out.println("✅ Rule PASSED!");
} catch (RuntimeException e) {
    System.out.println("❌ Rule FAILED: " + e.getMessage());
}
```

---

## 6. Multiple Rules (ALL Condition)

The `AllCondition` means **ALL rules must pass**:

```java
List<RulesLoader.Rule> rules = List.of(
    new RulesLoader.Rule("$.firstName", "equals", "A"),
    new RulesLoader.Rule("$.age", "greaterthan", "18"),
    new RulesLoader.Rule("$.status", "equals", "active")
);

// ALL three rules must pass, or it throws an exception
condition.execute(context, rules);
```

---

## 7. Testing

✅ **All 5 tests passed successfully:**
1. Simple rule with firstName equals "A" ✅
2. Rule fails when firstName is "B" ✅
3. Nested path navigation ✅
4. Multiple rules (all pass) ✅
5. Multiple rules (one fails) ✅

---

## Summary

**To check if a rule is correct:**

1. **Parse your JSON** into `JsonNode`
2. **Extract the value** using the path (e.g., `$.firstName`)
3. **Apply the operator** (e.g., `equals`, `contains`, `greaterthan`)
4. **Return TRUE/FALSE** based on the comparison

The implementation handles:
- ✅ Top-level fields: `$.firstName`
- ✅ Nested fields: `$.objectOne.name.firstName`
- ✅ Multiple rules (ALL must pass)
- ✅ Dynamic JSON structures (no fixed schema needed)
- ✅ Various operators (equals, contains, greater than, etc.)

**Result:**
- If all rules pass → Method completes successfully
- If any rule fails → Throws `RuntimeException` with details


# Python Code Sandbox

A Flask-based sandbox environment for executing Python code with data analysis capabilities and process isolation.

## Features

- Execute Python code via HTTP API
- Process isolation for each request
- Memory safety between requests
- Configurable execution timeout
- Pre-installed libraries support
- Thread-safe code execution

## Setup

1. Configure environment variables in `.env`:
```env
EXECUTION_TIMEOUT=30
PREINSTALL_LIBS="pandas numpy matplotlib scikit-learn"
```

2. Build image and run the Docker container:
```bash
docker build -t python-sandbox .
docker run -p 8080:8080 -v $(pwd):/app python-sandbox
```

## API Usage

### Simple Example
```bash
curl -X POST http://localhost:8080/api/execute \
-H "Content-Type: application/json" \
-d '{"code": "print(\"Hello, World!\")"}'
```

### Data Analysis Example
```bash
curl -X POST http://localhost:8080/api/execute \
-H "Content-Type: application/json" \
-d '{"code": "import pandas as pd\nimport numpy as np\n\n# Create sample data\ndata = {\n    \"date\": pd.date_range(start=\"2024-01-01\", periods=10),\n    \"sales\": np.random.randint(100, 1000, 10),\n    \"category\": np.random.choice([\"A\", \"B\", \"C\"], 10)\n}\n\n# Create DataFrame\ndf = pd.DataFrame(data)\n\n# Basic analysis\nprint(\"\\nDaily Sales Summary:\")\nprint(df.describe())\n\n# Category analysis\nprint(\"\\nSales by Category:\")\nprint(df.groupby(\"category\")[\"sales\"].agg([\"mean\", \"sum\"]))\n\n# Find best performing day\nbest_day = df.loc[df[\"sales\"].idxmax()]\nprint(f\"\\nBest Sales Day:\\n{best_day}\")"}'
```

Expected output:
```
Daily Sales Summary:
              sales
count    10.000000
mean    534.600000
std     279.949062
min     121.000000
25%     288.250000
50%     565.500000
75%     746.500000
max     932.000000

Sales by Category:
         mean    sum
category            
A       432.5   865
B       589.2  2946
C       523.0  1569

Best Sales Day:
date        2024-01-07
sales             932
category           B
Name: 6, dtype: object
```

## Configuration

### Environment Variables
- `EXECUTION_TIMEOUT`: Maximum execution time in seconds
- `PREINSTALL_LIBS`: Space-separated list of Python packages to pre-install

### Pre-installed Libraries
Default data science stack:
- pandas: Data manipulation and analysis
- numpy: Numerical computing
- matplotlib: Data visualization
- scikit-learn: Machine learning

## Error Handling

The API returns JSON responses with status and output:
```json
{
    "status": "Success|Error",
    "output": "Execution output or error message"
}
```

Common errors:
- Timeout exceeded
- Import errors
- Syntax errors
- Runtime errors

## Security Features

- Process isolation: Each request runs in a separate process
- Memory safety: No shared memory between requests
- Sandbox user: Non-root execution in Docker
- Clean namespace: Fresh environment for each execution
- Process cleanup: Automatic termination and resource cleanup

## Project Structure

```
.
├── Dockerfile
├── .env
├── app.py
└── README.md
```

## License

MIT License

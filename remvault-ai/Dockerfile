FROM python:3.11-slim

WORKDIR /app

# Install GCC and build tools needed to compile llama-cpp-python
RUN apt-get update && apt-get install -y build-essential gcc g++

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the source code
COPY . .

EXPOSE 8000

# Run Uvicorn bound to 0.0.0.0 so Docker can expose the port
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
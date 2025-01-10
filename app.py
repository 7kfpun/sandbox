from flask import Flask, request, jsonify
import io
import contextlib
import traceback
import multiprocessing
from multiprocessing import Process, Queue
import os
from dotenv import load_dotenv

load_dotenv()
EXECUTION_TIMEOUT = int(os.getenv("EXECUTION_TIMEOUT", 30))

app = Flask(__name__)


def execute_code_in_process(code, result_queue):
    # Create new namespace for each execution
    namespace = {}
    output = io.StringIO()

    try:
        with contextlib.redirect_stdout(output):
            with contextlib.redirect_stderr(output):
                exec(code, namespace)
        result_queue.put({"status": "Success", "output": output.getvalue()})
    except Exception as e:
        error_msg = f"{type(e).__name__}: {str(e)}\n"
        error_msg += traceback.format_exc()
        result_queue.put({"status": "Error", "output": error_msg})
    finally:
        output.close()
        # Clean up namespace
        namespace.clear()


def execute_code_with_timeout(code):
    result_queue = Queue()

    # Create new process for execution
    process = Process(target=execute_code_in_process, args=(code, result_queue))

    try:
        process.start()
        # Wait for result with timeout
        process.join(timeout=EXECUTION_TIMEOUT)

        if process.is_alive():
            process.terminate()
            process.join()
            return {
                "status": "Error",
                "output": f"Timeout Error: Code execution exceeded {EXECUTION_TIMEOUT} seconds",
            }

        if not result_queue.empty():
            return result_queue.get()
        return {"status": "Error", "output": "No result produced"}

    finally:
        # Ensure process is terminated and cleaned up
        if process.is_alive():
            process.terminate()
            process.join()
        # Clean up the queue
        while not result_queue.empty():
            _ = result_queue.get()


@app.route("/api/execute", methods=["POST"])
def execute_code():
    if not request.is_json:
        return jsonify({"status": "Error", "output": "Request must be JSON"}), 400

    code = request.json.get("code")
    if not code:
        return jsonify({"status": "Error", "output": "No code provided"}), 400

    result = execute_code_with_timeout(code)
    return jsonify(result)


if __name__ == "__main__":
    # Set start method to 'fork' for better performance on Unix
    if hasattr(multiprocessing, "set_start_method"):
        try:
            multiprocessing.set_start_method("fork")
        except RuntimeError:
            pass
    app.run(host="0.0.0.0", port=8080, debug=True, use_reloader=True)

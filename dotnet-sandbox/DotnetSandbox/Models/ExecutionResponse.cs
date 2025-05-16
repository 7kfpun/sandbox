namespace DotnetSandbox.Models;

public class ExecutionResponse
{
    public string Status { get; set; }
    public string Output { get; set; }

    public ExecutionResponse(string status, string output)
    {
        Status = status;
        Output = output;
    }
}

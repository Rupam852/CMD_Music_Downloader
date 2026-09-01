import sys
from rich.progress import ProgressColumn
from rich.text import Text

# Ensure UTF-8 output encoding for Windows CMD / PowerShell
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

class ClassicBoxBarColumn(ProgressColumn):
    """
    Classic Box-style Progress Bar: [████████████░░░░░░░░░░] from 0% to 100%
    """
    def __init__(self, bar_width: int = 25, table_column=None):
        self.bar_width = bar_width
        super().__init__(table_column=table_column)

    def render(self, task):
        total = task.total
        completed = task.completed or 0
        
        if total is None or total <= 0:
            ratio = 0.0
        else:
            ratio = min(1.0, max(0.0, completed / total))

        filled = int(ratio * self.bar_width)
        empty = max(0, self.bar_width - filled)

        bar_text = Text()
        bar_text.append("[", style="bold cyan")
        bar_text.append("█" * filled, style="bold green")
        bar_text.append("░" * empty, style="dim white")
        bar_text.append("]", style="bold cyan")
        return bar_text

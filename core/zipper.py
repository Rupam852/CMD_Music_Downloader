import os
import sys
import shutil
import zipfile
from pathlib import Path
from rich.progress import Progress, SpinnerColumn, TextColumn, TimeRemainingColumn
from rich.console import Console
from core.progress import ClassicBoxBarColumn

# Ensure UTF-8 output encoding
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

def zip_folder(
    source_dir: str | Path,
    output_zip: str | Path = None,
    delete_source: bool = True,
    console: Console = None
) -> Path:
    """
    Compresses all files in source_dir into a zip file with a classic box progress bar: [██████░░░░] 0% to 100%.
    """
    source_dir = Path(source_dir)
    if not source_dir.exists() or not source_dir.is_dir():
        raise ValueError(f"Directory '{source_dir}' does not exist.")

    if output_zip is None:
        output_zip = source_dir.parent / f"{source_dir.name}.zip"
    else:
        output_zip = Path(output_zip)

    files_to_zip = [f for f in source_dir.rglob("*") if f.is_file()]

    if not files_to_zip:
        raise ValueError(f"No files found in '{source_dir}' to zip.")

    output_zip.parent.mkdir(parents=True, exist_ok=True)

    if console is None:
        console = Console()

    console.print(f"\n[bold cyan]🗜️ Packaging {len(files_to_zip)} files into ZIP archive...[/bold cyan]")

    with Progress(
        SpinnerColumn(),
        TextColumn("[bold yellow]{task.fields[title]}[/bold yellow]"),
        ClassicBoxBarColumn(bar_width=25),
        TextColumn("[bold green]{task.percentage:>3.0f}%[/bold green]"),
        TextColumn("({task.completed}/{task.total} files)"),
        TimeRemainingColumn(),
        console=console
    ) as progress:
        zip_task = progress.add_task(
            "zip",
            total=len(files_to_zip),
            title="Starting compression..."
        )

        with zipfile.ZipFile(output_zip, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as zip_file:
            for file in files_to_zip:
                display_name = (file.name[:25] + "..") if len(file.name) > 25 else file.name
                progress.update(zip_task, title=f"🗜️ Zipping: {display_name}")
                archive_name = file.relative_to(source_dir)
                zip_file.write(file, archive_name)
                progress.advance(zip_task)

        progress.update(zip_task, title="[bold green]✅ Compression Complete![/bold green]")

    # Delete uncompressed source directory to save disk space if requested
    if delete_source:
        try:
            shutil.rmtree(source_dir)
            console.print(f"[dim yellow]🧹 Cleaned up temporary unzipped folder to save disk space.[/dim yellow]")
        except Exception as e:
            console.print(f"[dim red]Notice: Could not remove raw folder: {e}[/dim red]")

    size_mb = output_zip.stat().st_size / (1024 * 1024)
    console.print(f"[bold green]✨ Successfully created ZIP archive ({size_mb:.2f} MB):[/bold green] [yellow]{output_zip}[/yellow]\n")

    return output_zip

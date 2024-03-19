function ajustarAltura(elemento) {
    const alturaMaxima = 300;
    elemento.style.height = "auto";
    elemento.style.height = Math.min(elemento.scrollHeight, alturaMaxima) + "px";
}
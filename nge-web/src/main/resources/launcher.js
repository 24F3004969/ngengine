import {main} from "./webapp.js";

function init(demo) {
    document.querySelectorAll('button#startJme').forEach((btn) => {
    btn.style.display = 'none';
    });
    const canvasJme = document.querySelector('canvas#jme');
    canvasJme.style.display = 'block';
    main([demo]);
}

window.addEventListener('load', () => {
    document.querySelectorAll('button#startJme').forEach((btn) => {
        const demo = btn.getAttribute("demo");
        btn.addEventListener('click', () => {
            init(demo);
        });
    });

});
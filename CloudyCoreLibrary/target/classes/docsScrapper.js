let doc = document.getElementById("class-summary");
let names = doc.getElementsByClassName("col-first class-summary");
let resultCode = "";
for (let i = 0; i < names.length; ++i) {
    let a = names[i].getElementsByTagName("a")[0];
    let className = a.innerHTML;
    resultCode += "@EventHandler\npublic void on" + className + "(" + className + " e) {\n"
        + "\tcallEventListeners(e);\n"
        + "}\n\n";
}
console.log(resultCode);

// ------------

// let doc = document.getElementsByClassName("typeSummary")[0];
// let names = doc.getElementsByClassName("colFirst");
// let resultCode = "";
// for (let i = 0; i < names.length; i++) {
//     let scope=names[i].getAttribute("scope");
//     if (scope !== "row")
//         continue;
//     let a = names[i].getElementsByTagName("a")[0];
//     let className = a.innerHTML;
//     resultCode += "@EventHandler\npublic void on" + className + "(" + className + " e) {\n"
//         + "\tcallEventListeners(e);\n"
//         + "}\n\n";
// }
// console.log(resultCode);
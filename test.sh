for file in ./src/test/resources/scenarios/*;
do
    # echo $(basename -- "${file%.*}")
    ./colreg-classifier.sh -f "${file}" -o "./src/test/resources/classification-results/$(basename -- "${file%.*}")-result.json";
done

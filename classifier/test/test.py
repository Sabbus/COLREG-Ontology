import os
import unittest

from classifier.classifier import Classifier

SCRIPT_DIR = os.path.dirname(__file__)

class ClassificationTest(unittest.TestCase):
    classifier = Classifier()
    # @unittest.skip("a")
    def test_cornerstone_scenarios(self):
        """
        Test to classify the cornerstone scenarios.
        """
        scenarios = ["head_on.json",
                     "crossing_giveway.json",
                     "crossing_standon.json",
                     "overtaking_giveway.json",
                     "overtaking_standon.json"]

        os_behaviours = ['alter_course',
                         'alter_course',
                         'keep_course',
                         'alter_course',
                         'keep_course']

        ts_behaviours = ['alter_course',
                         'keep_course',
                         'alter_course',
                         'keep_course',
                         'alter_course']

        for os_behaviour, ts_behaviour, scenario in zip(os_behaviours, ts_behaviours, scenarios):
            with self.subTest(i=scenario):
                situation = SCRIPT_DIR + '/scenarios/' + scenario

                result = self.classifier.classify(situation)

                self.assertEqual(result['own-ship']['behaviour'], os_behaviour)
                self.assertEqual(result['target-ship']['behaviour'], ts_behaviour)

    # @unittest.skip("a")
    def test_sailing_vessel_scenarios(self):
        """
        Test to classify sailing vessel encounters.
        """
        scenarios = ["sailing_different_side_giveway.json",
                     "sailing_different_side_standon.json",
                     "sailing_same_side_giveway.json",
                     "sailing_same_side_standon.json"]

        os_behaviours = ['alter_course',
                         'keep_course', 'alter_course',
                         'keep_course']

        ts_behaviours = ['keep_course',
                         'alter_course',
                         'keep_course',
                         'alter_course']

        for os_behaviour, ts_behaviour, scenario in zip(os_behaviours, ts_behaviours, scenarios):
            with self.subTest(i=scenario):
                situation = SCRIPT_DIR + '/scenarios/' + scenario

                result = self.classifier.classify(situation)

                self.assertEqual(result['own-ship']['behaviour'], os_behaviour)
                self.assertEqual(result['target-ship']['behaviour'], ts_behaviour)

    # @unittest.skip("a")
    def test_different_vessel_scenarios(self):
        """
        Test to classify different vessel encounter.
        """
        scenarios = ["diff_pdv_sv.json",
                     "diff_pdv_veif.json",
                     "diff_pdv_vrihatm.json",
                     "diff_pdv_vcbhd.json",
                     "diff_pdv_vnuc.json",
                     "diff_sv_veif.json",
                     "diff_sv_vrihatm.json",
                     "diff_sv_vcbhd.json",
                     "diff_sv_vnuc.json",
                     "diff_veif_vrihatm.json",
                     "diff_veif_vcbhd.json",
                     "diff_veif_vnuc.json"]

        for scenario in scenarios:
            with self.subTest(i=scenario):
                situation = SCRIPT_DIR + '/scenarios/' + scenario

                result = self.classifier.classify(situation)

                self.assertEqual(result['own-ship']['behaviour'], 'alter_course')
                self.assertEqual(result['target-ship']['behaviour'], 'keep_course')

    # @unittest.skip("a")
    def test_lights(self):
        """
        Test to classify light configurations.
        """
        scenarios = ["lights_headon.json",
                     "lights_crossing.json",
                     "lights_sv_ahead.json",
                     "lights_sv_pts.json",
                     "lights_sv_stb.json",
                     "lights_veif_ahead.json",
                     "lights_veif_pts.json",
                     "lights_veif_stb.json",
                     "lights_vrihatm_ahead.json",
                     "lights_vrihatm_pts.json",
                     "lights_vrihatm_stb.json",
                     "lights_vcbhd_ahead.json",
                     "lights_vcbhd_pts.json",
                     "lights_vcbhd_stb.json",
                     "lights_vnuc_ahead.json",
                     "lights_vnuc_pts.json",
                     "lights_vnuc_stb.json"]

        categories = ["PowerDrivenVessel",
                      "PowerDrivenVessel",
                      "SailingVessel",
                      "SailingVessel",
                      "SailingVessel",
                      "VesselEngagedInFishing",
                      "VesselEngagedInFishing",
                      "VesselEngagedInFishing",
                      "VesselRestrictedInHerAbilityToManoeuvre",
                      "VesselRestrictedInHerAbilityToManoeuvre",
                      "VesselRestrictedInHerAbilityToManoeuvre",
                      "VesselConstrainedByHerDraught",
                      "VesselConstrainedByHerDraught",
                      "VesselConstrainedByHerDraught",
                      "VesselNotUnderCommand",
                      "VesselNotUnderCommand",
                      "VesselNotUnderCommand"]

        scen_categories = ["HeadOn",
                           "Crossing",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter"]

        for scenario, category, scen_category in zip(scenarios, categories, scen_categories):
            with self.subTest(i=scenario):
                situation = SCRIPT_DIR + '/scenarios/' + scenario

                result = self.classifier.classify(situation)

                self.assertEqual(result['own-ship']['behaviour'], 'alter_course')
                if scenarios.index(scenario) == 0:
                    self.assertEqual(result['target-ship']['behaviour'], 'alter_course')
                else:
                    self.assertEqual(result['target-ship']['behaviour'], 'keep_course')
                self.assertEqual(result['target-ship']['category'], category)
                self.assertEqual(result['situation-category'], scen_category)

    def test_shapes(self):
        """
        Test to classify shape configurations.
        """
        scenarios = ["shapes_veif.json",
                     "shapes_vrihatm.json",
                     "shapes_vcbhd.json",
                     "shapes_vnuc.json"]

        categories = ["VesselEngagedInFishing",
                      "VesselRestrictedInHerAbilityToManoeuvre",
                      "VesselConstrainedByHerDraught",
                      "VesselNotUnderCommand"]

        scen_categories = ["DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter",
                           "DifferentVesselEncounter"]

        for scenario, category, scen_category in zip(scenarios, categories, scen_categories):
            with self.subTest(i=scenario):
                situation = SCRIPT_DIR + '/scenarios/' + scenario

                result = self.classifier.classify(situation)

                self.assertEqual(result['own-ship']['behaviour'], 'alter_course')
                self.assertEqual(result['target-ship']['behaviour'], 'keep_course')
                self.assertEqual(result['target-ship']['category'], category)
                self.assertEqual(result['situation-category'], scen_category)

